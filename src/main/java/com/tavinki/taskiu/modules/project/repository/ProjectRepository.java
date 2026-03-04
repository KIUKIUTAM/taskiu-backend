package com.tavinki.taskiu.modules.project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tavinki.taskiu.modules.project.entity.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {

    /**
     * Find a project by its unique public Project ID.
     */
    Optional<Project> findByProjectId(String projectId);

    /**
     * Check if a project exists by its public Project ID.
     */
    boolean existsByProjectId(String projectId);

    /**
     * Find all projects belonging to a specific Team.
     * Since Project has @SQLRestriction("archived = false"), this will only return active projects.
     */
    List<Project> findByTeamId(String teamId);
    
    /**
     * Find a project by ID and Team ID (useful for security checks to ensure project belongs to team).
     */
    Optional<Project> findByIdAndTeamId(String id, String teamId);
}
