package com.tavinki.taskiu.modules.teams.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tavinki.taskiu.modules.teams.entity.Team;

@Repository
public interface TeamRepository extends JpaRepository<Team, String> {

    /**
     * Find a team by its unique public Team ID.
     */
    Optional<Team> findByTeamId(String teamId);

    /**
     * Check if a team exists by its public Team ID.
     * Useful for validation to ensure uniqueness before creation.
     */
    boolean existsByTeamId(String teamId);
}
