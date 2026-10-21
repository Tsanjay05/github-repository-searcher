package com.freightfox.githubsearcher.repository;

import com.freightfox.githubsearcher.entity.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositoryJpaRepository extends JpaRepository<RepositoryEntity, Long>, JpaSpecificationExecutor<RepositoryEntity> {

    // Used for upsert matching by GitHub's unique repository ID
    Optional<RepositoryEntity> findByGithubId(Long githubId);

    // Efficient bulk lookup for batches of incoming GitHub IDs
    List<RepositoryEntity> findByGithubIdIn(List<Long> githubIds);
}