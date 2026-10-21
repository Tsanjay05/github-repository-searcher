package com.freightfox.githubsearcher.service;

import com.freightfox.githubsearcher.dto.*;
import com.freightfox.githubsearcher.dto.github.GitHubItem;
import com.freightfox.githubsearcher.dto.github.GitHubSearchResponse;
import com.freightfox.githubsearcher.entity.RepositoryEntity;
import com.freightfox.githubsearcher.repository.RepositoryJpaRepository;
import com.freightfox.githubsearcher.repository.RepositorySpecifications;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RepositoryService {

    private final GitHubClient gitHubClient;
    private final RepositoryJpaRepository jpaRepository;

    public RepositoryService(GitHubClient gitHubClient, RepositoryJpaRepository jpaRepository) {
        this.gitHubClient = gitHubClient;
        this.jpaRepository = jpaRepository;
    }

    @Transactional
    public SearchResponse searchAndSave(SearchRequest request) {
        GitHubSearchResponse response = gitHubClient.searchRepositories(
                request.query(), request.language(), request.sort());

        if (response == null || response.items() == null || response.items().isEmpty()) {
            return new SearchResponse("No repositories found", List.of());
        }

        // Fetch existing records in batch to check for duplicates (Upsert logic)
        List<Long> incomingGithubIds = response.items().stream().map(GitHubItem::id).toList();
        Map<Long, RepositoryEntity> existingMap = jpaRepository.findByGithubIdIn(incomingGithubIds)
                .stream()
                .collect(Collectors.toMap(RepositoryEntity::getGithubId, Function.identity()));

        List<RepositoryEntity> entitiesToSave = new ArrayList<>();

        for (GitHubItem item : response.items()) {
            // If repository exists, update it; otherwise create a new record
            RepositoryEntity entity = existingMap.getOrDefault(item.id(), new RepositoryEntity());

            entity.setGithubId(item.id());
            entity.setName(item.name());
            entity.setDescription(item.description());
            entity.setOwner(item.owner() != null ? item.owner().login() : null);
            entity.setLanguage(item.language());
            entity.setStars(item.stars() != null ? item.stars() : 0);
            entity.setForks(item.forks() != null ? item.forks() : 0);
            entity.setLastUpdated(item.updatedAt());

            entitiesToSave.add(entity);
        }

        List<RepositoryEntity> saved = jpaRepository.saveAll(entitiesToSave);

        List<RepositoryDto> dtos = saved.stream()
                .map(this::mapToDto)
                .toList();

        return new SearchResponse("Repositories fetched and saved successfully", dtos);
    }

    @Transactional(readOnly = true)
    public RepositoryListResponse getStoredRepositories(String language, Integer minStars, String sortBy) {
        Specification<RepositoryEntity> spec = Specification
                .where(RepositorySpecifications.hasLanguage(language))
                .and(RepositorySpecifications.hasMinStars(minStars));

        Sort sort = resolveSort(sortBy);
        List<RepositoryEntity> entities = jpaRepository.findAll(spec, sort);

        return new RepositoryListResponse(entities.stream().map(this::mapToDto).toList());
    }

    private Sort resolveSort(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "stars"); // default is stars
        }
        return switch (sortBy.toLowerCase().trim()) {
            case "forks" -> Sort.by(Sort.Direction.DESC, "forks");
            case "updated" -> Sort.by(Sort.Direction.DESC, "lastUpdated");
            default -> Sort.by(Sort.Direction.DESC, "stars");
        };
    }

    private RepositoryDto mapToDto(RepositoryEntity entity) {
        return new RepositoryDto(
                entity.getGithubId(),
                entity.getName(),
                entity.getDescription(),
                entity.getOwner(),
                entity.getLanguage(),
                entity.getStars(),
                entity.getForks(),
                entity.getLastUpdated()
        );
    }
}