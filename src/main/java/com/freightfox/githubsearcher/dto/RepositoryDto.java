package com.freightfox.githubsearcher.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

public record RepositoryDto(
    Long id,
    String name,
    String description,
    String owner,
    String language,
    Integer stars,
    Integer forks,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Instant lastUpdated
) {}