package com.freightfox.githubsearcher.service;

import com.freightfox.githubsearcher.dto.github.GitHubSearchResponse;
import com.freightfox.githubsearcher.exception.GitHubApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GitHubClient {

    private final RestClient restClient;

    public GitHubClient(
            @Value("${github.api.base-url}") String baseUrl,
            @Value("${github.api.token:}") String token) {
        
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("User-Agent", "GitHub-Searcher-App");

        if (token != null && !token.isBlank()) {
            builder.defaultHeader("Authorization", "Bearer " + token);
        }

        this.restClient = builder.build();
    }

    public GitHubSearchResponse searchRepositories(String query, String language, String sort) {
        StringBuilder qBuilder = new StringBuilder(query);
        if (language != null && !language.isBlank()) {
            qBuilder.append(" language:").append(language.trim());
        }

        String ghSort = mapSortField(sort);

        return restClient.get()
                .uri(uriBuilder -> {
                    var ub = uriBuilder
                            .path("/search/repositories")
                            .queryParam("q", qBuilder.toString());
                    if (ghSort != null) {
                        ub.queryParam("sort", ghSort);
                    }
                    return ub.build();
                })
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, resp) -> {
                    if (resp.getStatusCode().value() == 403) {
                        throw new GitHubApiException("GitHub API rate limit exceeded or access forbidden.");
                    }
                    throw new GitHubApiException("GitHub API failed with status: " + resp.getStatusCode());
                })
                .body(GitHubSearchResponse.class);
    }

    private String mapSortField(String sort) {
        if (sort == null) return null;
        return switch (sort.toLowerCase().trim()) {
            case "stars" -> "stars";
            case "forks" -> "forks";
            case "updated" -> "updated";
            default -> null;
        };
    }
}