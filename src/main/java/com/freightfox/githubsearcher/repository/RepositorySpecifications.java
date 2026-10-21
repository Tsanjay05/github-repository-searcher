package com.freightfox.githubsearcher.repository;

import com.freightfox.githubsearcher.entity.RepositoryEntity;
import org.springframework.data.jpa.domain.Specification;

public class RepositorySpecifications {

    // Filter by programming language (case-insensitive)
    public static Specification<RepositoryEntity> hasLanguage(String language) {
        return (root, query, criteriaBuilder) -> {
            if (language == null || language.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(
                criteriaBuilder.lower(root.get("language")),
                language.toLowerCase().trim()
            );
        };
    }

    // Filter by minimum star count
    public static Specification<RepositoryEntity> hasMinStars(Integer minStars) {
        return (root, query, criteriaBuilder) -> {
            if (minStars == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("stars"), minStars);
        };
    }
}