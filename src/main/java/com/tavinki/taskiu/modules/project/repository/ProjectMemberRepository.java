package com.tavinki.taskiu.modules.project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tavinki.taskiu.modules.project.entity.ProjectMember;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, String> {

    /**
     * Find all members of a specific project.
     */
    List<ProjectMember> findByProjectId(String projectId);

    /**
     * Find all projects a specific user is part of.
     */
    List<ProjectMember> findByUserId(String userId);

    /**
     * Find a specific membership record (e.g., to check role or remove member).
     */
    Optional<ProjectMember> findByProjectIdAndUserId(String projectId, String userId);

    /**
     * Check if a user is a member of a project.
     */
    boolean existsByProjectIdAndUserId(String projectId, String userId);

    /**
     * Delete a member from a project.
     */
    void deleteByProjectIdAndUserId(String projectId, String userId);
}
