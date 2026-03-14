package com.tavinki.taskiu.modules.project.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tavinki.taskiu.modules.project.entity.Project;
import com.tavinki.taskiu.modules.project.repository.ProjectRepository;
import com.tavinki.taskiu.modules.teams.entity.Team;
import com.tavinki.taskiu.modules.teams.service.TeamService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TeamService teamService;
    private final ProjectPictureMinioService projectPictureMinioService;

    @Transactional
    public Project createProject(String teamId, String name, String description, MultipartFile file, String creatorEmail) {
        Team team = teamService.getTeamByPublicId(teamId);
        
        String projectId = generateUniqueProjectId();
        String pictureKey = null;

        if (file != null && !file.isEmpty()) {
            try {
                pictureKey = projectPictureMinioService.uploadProjectPicture(
                    projectId, 
                    file.getInputStream(), 
                    file.getSize(),
                    file.getContentType(), 
                    file.getOriginalFilename()
                );
            } catch (Exception e) {
                log.error("Failed to upload project picture for project: {}", projectId, e);
            }
        }
        
        Project project = Project.builder()
                .projectId(projectId)
                .projectName(name)
                .projectDescription(description)
                .projectPicture(pictureKey)
                .team(team)
                .archived(false)
                .build();
        
        project = projectRepository.save(project);
        
        if (project.getProjectPicture() != null) {
            project.setProjectPictureUrl(projectPictureMinioService.getPresignedUrl(project.getProjectPicture()));
        }
        
        log.info("Created project: {} ({}) in team: {} by user: {}", name, projectId, teamId, creatorEmail);
        return project;
    }

    public Project getProjectByPublicId(String projectId) {
        Project project = projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with ID: " + projectId));
        
        if (project.getProjectPicture() != null) {
            project.setProjectPictureUrl(projectPictureMinioService.getPresignedUrl(project.getProjectPicture()));
        }
        
        return project;
    }

    public List<Project> getTeamProjects(String teamId) {
        Team team = teamService.getTeamByPublicId(teamId);
        List<Project> projects = projectRepository.findByTeamId(team.getId());
        
        projects.forEach(project -> {
            if (project.getProjectPicture() != null) {
                project.setProjectPictureUrl(projectPictureMinioService.getPresignedUrl(project.getProjectPicture()));
            }
        });
        
        return projects;
    }

    @Transactional
    public Project updateProject(String projectId, String name, String description, MultipartFile file) {
        Project project = getProjectByPublicId(projectId);
        
        if (name != null && !name.isBlank()) {
            project.setProjectName(name);
        }
        if (description != null) {
            project.setProjectDescription(description);
        }
        
        if (file != null && !file.isEmpty()) {
            try {
                if (project.getProjectPicture() != null) {
                    try {
                        projectPictureMinioService.delete(project.getProjectPicture());
                    } catch (Exception e) {
                        log.warn("Failed to delete old project picture: {}", project.getProjectPicture());
                    }
                }
                
                String pictureKey = projectPictureMinioService.uploadProjectPicture(
                    projectId, 
                    file.getInputStream(), 
                    file.getSize(),
                    file.getContentType(), 
                    file.getOriginalFilename()
                );
                project.setProjectPicture(pictureKey);
            } catch (Exception e) {
                log.error("Failed to upload project picture for project: {}", projectId, e);
                throw new RuntimeException("Failed to upload project picture");
            }
        }
        
        project = projectRepository.save(project);
        
        if (project.getProjectPicture() != null) {
            project.setProjectPictureUrl(projectPictureMinioService.getPresignedUrl(project.getProjectPicture()));
        }
        
        return project;
    }

    @Transactional
    public void deleteProject(String projectId) {
        Project project = getProjectByPublicId(projectId);
        
        if (project.getProjectPicture() != null) {
            try {
                projectPictureMinioService.delete(project.getProjectPicture());
            } catch (Exception e) {
                log.warn("Failed to delete project picture during project deletion: {}", project.getProjectPicture());
            }
        }
        
        projectRepository.delete(project);
        log.info("Deleted project: {}", projectId);
    }

    private String generateUniqueProjectId() {
        String projectId;
        do {
            projectId = "prj_" + UUID.randomUUID().toString().substring(0, 8);
        } while (projectRepository.existsByProjectId(projectId));
        return projectId;
    }
}
