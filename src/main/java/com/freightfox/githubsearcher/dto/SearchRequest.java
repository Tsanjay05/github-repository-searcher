package com.freightfox.githubsearcher.dto;

import jakarta.validation.constraints.NotBlank;

public record SearchRequest(
    @NotBlank(message = "Query cannot be blank")
    String query,
    String language,
    String sort
) {}