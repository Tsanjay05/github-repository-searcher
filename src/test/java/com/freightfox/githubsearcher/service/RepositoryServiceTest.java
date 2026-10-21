package com.freightfox.githubsearcher.service;

import com.freightfox.githubsearcher.dto.*;
import com.freightfox.githubsearcher.dto.github.GitHubItem;
import com.freightfox.githubsearcher.dto.github.GitHubOwner;
import com.freightfox.githubsearcher.dto.github.GitHubSearchResponse;
import com.freightfox.githubsearcher.entity.RepositoryEntity;
import com.freightfox.githubsearcher.repository.RepositoryJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepositoryServiceTest {

    @Mock
    private GitHubClient gitHubClient;

    @Mock
    private RepositoryJpaRepository jpaRepository;

    private RepositoryService repositoryService;

    @BeforeEach
    void setUp() {
        repositoryService = new RepositoryService(gitHubClient, jpaRepository);
    }

    @Test
    @DisplayName("searchAndSave should insert new repositories when not present in DB")
    void testSearchAndSave_NewRepositories() {
        GitHubItem item = new GitHubItem(
                99999L, "sample-repo", "A sample repository",
                new GitHubOwner("testuser"), "Java", 250, 45, Instant.now()
        );
        GitHubSearchResponse ghResponse = new GitHubSearchResponse(1, List.of(item));

        when(gitHubClient.searchRepositories("sample", "Java", "stars")).thenReturn(ghResponse);
        when(jpaRepository.findByGithubIdIn(List.of(99999L))).thenReturn(List.of());
        when(jpaRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SearchRequest request = new SearchRequest("sample", "Java", "stars");
        SearchResponse response = repositoryService.searchAndSave(request);

        assertNotNull(response);
        assertEquals("Repositories fetched and saved successfully", response.message());
        assertEquals(1, response.repositories().size());
        assertEquals("sample-repo", response.repositories().get(0).name());
        assertEquals(250, response.repositories().get(0).stars());
        verify(jpaRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("searchAndSave should update existing repository without duplicating (Upsert)")
    void testSearchAndSave_UpsertExisting() {
        Long existingGithubId = 12345L;
        GitHubItem updatedItem = new GitHubItem(
                existingGithubId, "updated-repo-name", "Updated description",
                new GitHubOwner("updateduser"), "Java", 600, 150, Instant.now()
        );
        GitHubSearchResponse ghResponse = new GitHubSearchResponse(1, List.of(updatedItem));

        RepositoryEntity existingEntity = new RepositoryEntity(
                existingGithubId, "old-name", "old-description",
                "olduser", "Java", 100, 20, Instant.now()
        );

        when(gitHubClient.searchRepositories("updated", "Java", "stars")).thenReturn(ghResponse);
        when(jpaRepository.findByGithubIdIn(List.of(existingGithubId))).thenReturn(List.of(existingEntity));
        when(jpaRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SearchRequest request = new SearchRequest("updated", "Java", "stars");
        SearchResponse response = repositoryService.searchAndSave(request);

        assertEquals(1, response.repositories().size());
        assertEquals("updated-repo-name", response.repositories().get(0).name());
        assertEquals(600, response.repositories().get(0).stars());
        assertEquals(150, response.repositories().get(0).forks());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RepositoryEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(jpaRepository).saveAll(captor.capture());
        assertEquals(1, captor.getValue().size());
        assertEquals(existingGithubId, captor.getValue().get(0).getGithubId());
    }

    @Test
    @DisplayName("searchAndSave should handle empty GitHub response gracefully")
    void testSearchAndSave_EmptyResult() {
        GitHubSearchResponse emptyResponse = new GitHubSearchResponse(0, List.of());
        when(gitHubClient.searchRepositories("nonexistent", "Java", "stars")).thenReturn(emptyResponse);

        SearchRequest request = new SearchRequest("nonexistent", "Java", "stars");
        SearchResponse response = repositoryService.searchAndSave(request);

        assertEquals("No repositories found", response.message());
        assertTrue(response.repositories().isEmpty());
        verify(jpaRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("getStoredRepositories should map entities to DTOs correctly")
    void testGetStoredRepositories() {
        RepositoryEntity entity = new RepositoryEntity(
                111L, "stored-repo", "description",
                "owner1", "Java", 300, 40, Instant.now()
        );

        when(jpaRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(entity));

        RepositoryListResponse response = repositoryService.getStoredRepositories("Java", 100, "stars");

        assertNotNull(response);
        assertEquals(1, response.repositories().size());
        assertEquals("stored-repo", response.repositories().get(0).name());
        assertEquals(300, response.repositories().get(0).stars());
    }
}