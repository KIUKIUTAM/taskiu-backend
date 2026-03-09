package com.tavinki.taskiu.modules.teams.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tavinki.taskiu.common.annotations.attribute.TeamId;
import com.tavinki.taskiu.common.annotations.role.RequireTeamRole;
import com.tavinki.taskiu.common.enums.role.TeamRole;
import com.tavinki.taskiu.modules.teams.dto.CreateTeamRequest;
import com.tavinki.taskiu.modules.teams.dto.InviteMemberRequest;
import com.tavinki.taskiu.modules.teams.dto.TeamMemberResponseDto;
import com.tavinki.taskiu.modules.teams.dto.TeamResponseDto;
import com.tavinki.taskiu.modules.teams.dto.UpdateTeamRequest;
import com.tavinki.taskiu.modules.teams.entity.Team;
import com.tavinki.taskiu.modules.teams.entity.TeamMember;
import com.tavinki.taskiu.modules.teams.mapper.TeamMapper;
import com.tavinki.taskiu.modules.teams.service.TeamService;
import com.tavinki.taskiu.modules.user.dto.UserResponseDto;
import com.tavinki.taskiu.modules.user.entity.User;
import com.tavinki.taskiu.modules.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<TeamResponseDto> createTeam(
            @Valid @RequestBody CreateTeamRequest request,
            @AuthenticationPrincipal UserResponseDto currentUser) {
        User user = userService.getUserById(currentUser.getId());
        Team team = teamService.createTeam(request.getName(), request.getDescription(), user);
        return ResponseEntity.ok(TeamMapper.toTeamDto(team));
    }

    @GetMapping("/{teamId}")
    @RequireTeamRole({TeamRole.OWNER, TeamRole.ADMIN, TeamRole.MEMBER})
    public ResponseEntity<TeamResponseDto> getTeam(
            @PathVariable @TeamId String teamId) {
        Team team = teamService.getTeamByPublicId(teamId);
        return ResponseEntity.ok(TeamMapper.toTeamDto(team));
    }

    @PutMapping("/{teamId}")
    @RequireTeamRole({TeamRole.OWNER, TeamRole.ADMIN})
    public ResponseEntity<TeamResponseDto> updateTeam(
            @PathVariable @TeamId String teamId,
            @Valid @RequestBody UpdateTeamRequest request) {
        Team updatedTeam = teamService.updateTeam(
                teamId,
                request.getName(),
                request.getDescription(),
                request.getPicture());
        return ResponseEntity.ok(TeamMapper.toTeamDto(updatedTeam));
    }

    @DeleteMapping("/{teamId}")
    @RequireTeamRole(TeamRole.OWNER)
    public ResponseEntity<Void> deleteTeam(
            @PathVariable @TeamId String teamId) {
        teamService.deleteTeam(teamId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{teamId}/members")
    @RequireTeamRole({TeamRole.OWNER, TeamRole.ADMIN, TeamRole.MEMBER})
    public ResponseEntity<List<TeamMemberResponseDto>> getTeamMembers(
            @PathVariable @TeamId String teamId) {
        List<TeamMember> members = teamService.getTeamMembers(teamId);
        List<TeamMemberResponseDto> response = members.stream()
                .map(TeamMapper::toTeamMemberDto)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{teamId}/members")
    @RequireTeamRole({TeamRole.OWNER, TeamRole.ADMIN})
    public ResponseEntity<TeamMemberResponseDto> addMember(
            @PathVariable @TeamId String teamId,
            @Valid @RequestBody InviteMemberRequest request) {
        TeamMember member = teamService.addMember(teamId, request.getEmail(), request.getRole());
        return ResponseEntity.ok(TeamMapper.toTeamMemberDto(member));
    }

    @DeleteMapping("/{teamId}/members/{userId}")
    @RequireTeamRole({TeamRole.OWNER, TeamRole.ADMIN})
    public ResponseEntity<Void> removeMember(
            @PathVariable @TeamId String teamId,
            @PathVariable String userId) {
        teamService.removeMember(teamId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<List<TeamMemberResponseDto>> getMyTeams(
            @AuthenticationPrincipal UserResponseDto currentUser) {
        List<TeamMember> userTeams = teamService.getUserTeams(currentUser.getId());
        List<TeamMemberResponseDto> response = userTeams.stream()
                .map(TeamMapper::toTeamMemberDto)
                .toList();
        return ResponseEntity.ok(response);
    }
}
