package com.tavinki.taskiu.modules.teams.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tavinki.taskiu.common.enums.role.TeamRole;
import com.tavinki.taskiu.modules.teams.entity.TeamMember;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, String> {

    /**
     * Find all members of a specific team (by Team DB UUID).
     */
    List<TeamMember> findByTeamId(String teamId);

    /**
     * Find all teams a specific user is part of (by User DB UUID).
     */
    List<TeamMember> findByUserId(String userId);

    /**
     * Find a specific membership record by Team ID and User ID.
     * Useful for checking role or updating membership.
     */
    Optional<TeamMember> findByTeamIdAndUserId(String teamId, String userId);

    /**
     * Check if a user is already a member of a team.
     * Useful for validation before adding a new member.
     */
    boolean existsByTeamIdAndUserId(String teamId, String userId);

    /**
     * Delete a specific membership record by Team ID and User ID.
     * Used when removing a member from a team.
     */
    void deleteByTeamIdAndUserId(String teamId, String userId);

    /**
     * Find all members of a team with a specific role.
     * Example: find all ADMIN members of a team.
     */
    List<TeamMember> findByTeamIdAndRole(String teamId, TeamRole role);

    /**
     * Count the number of members in a team.
     * Useful for displaying team size or enforcing member limits.
     */
    long countByTeamId(String teamId);
}
