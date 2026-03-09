package com.tavinki.taskiu.modules.teams.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tavinki.taskiu.common.enums.role.TeamRole;
import com.tavinki.taskiu.modules.teams.entity.TeamMember;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, String> {

    /**
     * Find all members of a specific team (by Team DB UUID).
     * JOIN FETCH to avoid LazyInitializationException.
     */
    @Query("SELECT tm FROM TeamMember tm JOIN FETCH tm.team JOIN FETCH tm.user WHERE tm.team.id = :teamId")
    List<TeamMember> findByTeamId(@Param("teamId") String teamId);

    /**
     * Find all teams a specific user is part of (by User DB UUID).
     * JOIN FETCH to avoid LazyInitializationException.
     */
    @Query("SELECT tm FROM TeamMember tm JOIN FETCH tm.team JOIN FETCH tm.user WHERE tm.user.id = :userId")
    List<TeamMember> findByUserId(@Param("userId") String userId);

    /**
     * Find a specific membership record by Team ID and User ID.
     * JOIN FETCH to avoid LazyInitializationException.
     */
    @Query("SELECT tm FROM TeamMember tm JOIN FETCH tm.team JOIN FETCH tm.user WHERE tm.team.id = :teamId AND tm.user.id = :userId")
    Optional<TeamMember> findByTeamIdAndUserId(@Param("teamId") String teamId, @Param("userId") String userId);

    /**
     * Check if a user is already a member of a team.
     */
    boolean existsByTeamIdAndUserId(String teamId, String userId);

    /**
     * Delete a specific membership record by Team ID and User ID.
     */
    void deleteByTeamIdAndUserId(String teamId, String userId);

    /**
     * Find all members of a team with a specific role.
     * JOIN FETCH to avoid LazyInitializationException.
     */
    @Query("SELECT tm FROM TeamMember tm JOIN FETCH tm.team JOIN FETCH tm.user WHERE tm.team.id = :teamId AND tm.role = :role")
    List<TeamMember> findByTeamIdAndRole(@Param("teamId") String teamId, @Param("role") TeamRole role);

    /**
     * Count the number of members in a team.
     */
    long countByTeamId(String teamId);
}
