package com.tavinki.taskiu.modules.teams.repository;


import java.util.List;
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

    /**
     * Find all teams belonging to a specific Company (by Company DB UUID).
     * Since Team has @SQLRestriction("archived = false"), only active teams are returned.
     */
    List<Team> findByCompanyId(String companyId);

    /**
     * Find a team by its public Team ID and Company ID.
     * Useful for security checks to ensure the team belongs to the company.
     */
    Optional<Team> findByTeamIdAndCompanyId(String teamId, String companyId);

    /**
     * Search for teams by name (case-insensitive partial match) within a company.
     * Example: "Dev" finds "DevOps", "Backend Dev", etc.
     */
    List<Team> findByTeamNameContainingIgnoreCaseAndCompanyId(String teamName, String companyId);
}
