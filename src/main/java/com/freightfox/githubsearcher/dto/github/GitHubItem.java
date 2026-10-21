package com.freightfox.githubsearcher.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record GitHubItem(
    Long id,
    String name,
    String description,
    GitHubOwner owner,
    String language,
    @JsonProperty("stargazers_count") Integer stars,
    @JsonProperty("forks_count") Integer forks,
    @JsonProperty("updated_at") Instant updatedAt
) {}