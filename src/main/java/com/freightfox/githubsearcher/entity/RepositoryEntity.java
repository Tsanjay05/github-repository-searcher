package com.freightfox.githubsearcher.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "repositories", indexes = {
    @Index(name = "idx_repo_language", columnList = "language"),
    @Index(name = "idx_repo_stars", columnList = "stars")
})
public class RepositoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "github_id", nullable = false, unique = true)
    private Long githubId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "owner_name")
    private String owner;

    private String language;

    private Integer stars;

    private Integer forks;

    @Column(name = "last_updated")
    private Instant lastUpdated;

    public RepositoryEntity() {}

    public RepositoryEntity(Long githubId, String name, String description, String owner,
                            String language, Integer stars, Integer forks, Instant lastUpdated) {
        this.githubId = githubId;
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.language = language;
        this.stars = stars;
        this.forks = forks;
        this.lastUpdated = lastUpdated;
    }

    public Long getId() { return id; }
    public Long getGithubId() { return githubId; }
    public void setGithubId(Long githubId) { this.githubId = githubId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public Integer getStars() { return stars; }
    public void setStars(Integer stars) { this.stars = stars; }
    public Integer getForks() { return forks; }
    public void setForks(Integer forks) { this.forks = forks; }
    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }
}