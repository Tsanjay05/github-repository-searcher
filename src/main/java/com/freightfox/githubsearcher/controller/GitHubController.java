package com.freightfox.githubsearcher.controller;

import com.freightfox.githubsearcher.dto.RepositoryListResponse;
import com.freightfox.githubsearcher.dto.SearchRequest;
import com.freightfox.githubsearcher.dto.SearchResponse;
import com.freightfox.githubsearcher.service.RepositoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/github")
@Tag(name = "GitHub Repository Searcher", description = "Endpoints to search GitHub repositories and retrieve stored results")
public class GitHubController {

    private final RepositoryService repositoryService;

    public GitHubController(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @PostMapping("/search")
    @Operation(summary = "Search GitHub and save repositories", description = "Fetches repositories from GitHub API and saves/updates them in PostgreSQL.")
    public ResponseEntity<SearchResponse> searchRepositories(@Valid @RequestBody SearchRequest request) {
        SearchResponse response = repositoryService.searchAndSave(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/repositories")
    @Operation(summary = "Retrieve stored repositories", description = "Retrieves saved repositories from PostgreSQL with optional filters and sorting.")
    public ResponseEntity<RepositoryListResponse> getRepositories(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Integer minStars,
            @RequestParam(required = false, defaultValue = "stars") String sort) {
        RepositoryListResponse response = repositoryService.getStoredRepositories(language, minStars, sort);
        return ResponseEntity.ok(response);
    }
}