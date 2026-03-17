package com.tavinki.taskiu.modules.teams.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tavinki.taskiu.common.enums.role.TeamRole;
import com.tavinki.taskiu.modules.teams.entity.Team;
import com.tavinki.taskiu.modules.teams.entity.TeamMember;
import com.tavinki.taskiu.modules.teams.repository.TeamMemberRepository;
import com.tavinki.taskiu.modules.teams.repository.TeamRepository;
import com.tavinki.taskiu.modules.user.entity.User;
import com.tavinki.taskiu.modules.user.repository.UserJpaRepository;
import com.tavinki.taskiu.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserJpaRepository userRepository;
    private final UserService userService;

    @Transactional
    public Team createTeam(String name, String description, String creatorUserId) {
        String teamId = generateUniqueTeamId();
        User user = userService.getUserById(creatorUserId);
        Team team = Team.builder()
                .teamId(teamId)
                .teamName(name)
                .teamDescription(description)
                .archived(false)
                .build();
        
        team = teamRepository.save(team);
        
        addMemberToTeam(team, user, TeamRole.OWNER);
        
        log.info("Created team: {} ({}) by user: {}", name, teamId, user.getEmail());
        return team;
    }

    public Team getTeamByPublicId(String teamId) {
        return teamRepository.findByTeamId(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found with ID: " + teamId));
    }

    @Transactional
    public Team updateTeam(String teamId, String name, String description, String picture) {
        Team team = getTeamByPublicId(teamId);
        
        if (name != null && !name.isBlank()) {
            team.setTeamName(name);
        }
        if (description != null) {
            team.setTeamDescription(description);
        }
        if (picture != null) {
            team.setTeamPicture(picture);
        }
        
        return teamRepository.save(team);
    }

    @Transactional
    public void deleteTeam(String teamId) {
        Team team = getTeamByPublicId(teamId);
        teamRepository.delete(team);
        
        List<TeamMember> members = teamMemberRepository.findByTeamId(team.getId());
        teamMemberRepository.deleteAll(members);
        
        log.info("Deleted team: {}", teamId);
    }

    @Transactional
    public TeamMember addMember(String teamId, String email, TeamRole role) {
        Team team = getTeamByPublicId(teamId);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        return addMemberToTeam(team, user, role);
    }

    private TeamMember addMemberToTeam(Team team, User user, TeamRole role) {
        if (teamMemberRepository.existsByTeamIdAndUserId(team.getId(), user.getId())) {
             throw new RuntimeException("User is already a member of this team");
        }
        
        TeamMember member = TeamMember.builder()
                .team(team)
                .user(user)
                .role(role)
                .archived(false)
                .build();
        
        return teamMemberRepository.save(member);
    }

    @Transactional
    public void removeMember(String teamId, String userId) {
        Team team = getTeamByPublicId(teamId);
        
        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(team.getId(), userId)
                .orElseThrow(() -> new RuntimeException("Member not found in team"));
                
        if (member.getRole() == TeamRole.OWNER) {
             long ownerCount = teamMemberRepository.findByTeamId(team.getId()).stream()
                     .filter(m -> m.getRole() == TeamRole.OWNER)
                     .count();
             if (ownerCount <= 1) {
                 throw new RuntimeException("Cannot remove the last owner of the team");
             }
        }

        teamMemberRepository.delete(member);
    }

    public List<TeamMember> getTeamMembers(String teamId) {
        Team team = getTeamByPublicId(teamId);
        return teamMemberRepository.findByTeamId(team.getId());
    }

    public List<TeamMember> getUserTeams(String userId) {
        return teamMemberRepository.findByUserId(userId);
    }

    private String generateUniqueTeamId() {
        String teamId;
        do {
            teamId = "tm_" + UUID.randomUUID().toString().substring(0, 8);
        } while (teamRepository.existsByTeamId(teamId));
        return teamId;
    }


    
}
