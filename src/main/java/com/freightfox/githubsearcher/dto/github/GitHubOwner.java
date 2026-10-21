package com.freightfox.githubsearcher.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubOwner(
    @JsonProperty("login") String login
) {}