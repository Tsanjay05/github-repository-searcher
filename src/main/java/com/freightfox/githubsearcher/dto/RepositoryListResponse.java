package com.freightfox.githubsearcher.dto;

import java.util.List;

public record RepositoryListResponse(
    List<RepositoryDto> repositories
) {}