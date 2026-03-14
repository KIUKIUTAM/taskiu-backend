package com.tavinki.taskiu.modules.project.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tavinki.taskiu.common.annotations.attribute.TeamId;
import com.tavinki.taskiu.common.annotations.role.RequireTeamRole;
import com.tavinki.taskiu.common.enums.role.TeamRole;
import com.tavinki.taskiu.modules.project.dto.CreateProjectRequest;
import com.tavinki.taskiu.modules.project.dto.UpdateProjectRequest;
import com.tavinki.taskiu.modules.project.entity.Project;
import com.tavinki.taskiu.modules.project.service.ProjectService;
import com.tavinki.taskiu.modules.user.dto.UserResponseDto;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/teams/{teamId}/projects")
@RequiredArgsConstructor
@Slf4j
public class ProjectController {

    private final ProjectService projectService;

    /**
     * Create a new project in a team with optional picture upload.
     * Requires OWNER or ADMIN role.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireTeamRole({TeamRole.OWNER, TeamRole.ADMIN})
    public ResponseEntity<Project> createProject(
            @PathVariable @TeamId String teamId,
            @Valid @RequestPart("data") CreateProjectRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal UserResponseDto currentUser) {
        
        Project project = projectService.createProject(teamId, request.getName(), request.getDescription(), file, currentUser.getEmail());
        
        return ResponseEntity.ok(project);
    }

    /**
     * List all projects for a team.
     * Requires being a member of the team.
     */
    @GetMapping
    @RequireTeamRole({TeamRole.OWNER, TeamRole.ADMIN, TeamRole.MEMBER})
    public ResponseEntity<List<Project>> getTeamProjects(@PathVariable @TeamId String teamId) {
        List<Project> projects = projectService.getTeamProjects(teamId);
        return ResponseEntity.ok(projects);
    }

    /**
     * Get details of a specific project.
     * Requires being a member of the team.
     */
    @GetMapping("/{projectId}")
    @RequireTeamRole({TeamRole.OWNER, TeamRole.ADMIN, TeamRole.MEMBER})
    public ResponseEntity<Project> getProject(
            @PathVariable @TeamId String teamId,
            @PathVariable String projectId) {
        Project project = projectService.getProjectByPublicId(projectId);
        return ResponseEntity.ok(project);
    }

    /**
     * Update project information with optional picture upload.
     * Requires OWNER or ADMIN role.
     */
    @PutMapping(value = "/{projectId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireTeamRole({TeamRole.OWNER, TeamRole.ADMIN})
    public ResponseEntity<Project> updateProject(
            @PathVariable @TeamId String teamId,
            @PathVariable String projectId,
            @Valid @RequestPart("data") UpdateProjectRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        
        Project updatedProject = projectService.updateProject(
                projectId, 
                request.getName(), 
                request.getDescription(), 
                file);
        
        return ResponseEntity.ok(updatedProject);
    }

    /**
     * Delete a project.
     * Requires OWNER role.
     */
    @DeleteMapping("/{projectId}")
    @RequireTeamRole(TeamRole.OWNER)
    public ResponseEntity<Void> deleteProject(
            @PathVariable @TeamId String teamId,
            @PathVariable String projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }
}
