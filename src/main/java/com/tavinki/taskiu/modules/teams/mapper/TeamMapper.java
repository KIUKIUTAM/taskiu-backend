package com.tavinki.taskiu.modules.teams.mapper;

import com.tavinki.taskiu.modules.teams.dto.TeamMemberResponseDto;
import com.tavinki.taskiu.modules.teams.dto.TeamMemberUserDto;
import com.tavinki.taskiu.modules.teams.dto.TeamResponseDto;
import com.tavinki.taskiu.modules.teams.entity.Team;
import com.tavinki.taskiu.modules.teams.entity.TeamMember;

public class TeamMapper {

    private TeamMapper() {
        // Private constructor to prevent instantiation
    }

    public static TeamResponseDto toTeamDto(Team team) {
        if (team == null) return null;
        return TeamResponseDto.builder()
                .id(team.getId())
                .teamId(team.getTeamId())
                .teamName(team.getTeamName())
                .teamDescription(team.getTeamDescription())
                .teamPicture(team.getTeamPicture())
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .build();
    }

    public static TeamMemberResponseDto toTeamMemberDto(TeamMember member) {
        if (member == null) return null;

        TeamMemberUserDto userDto = TeamMemberUserDto.builder()
                .id(member.getUser().getId())
                .username(member.getUser().getName())
                .email(member.getUser().getEmail())
                .avatar(member.getUser().getPicture())
                .build();

        return TeamMemberResponseDto.builder()
                .id(member.getId())
                .role(member.getRole())
                .team(toTeamDto(member.getTeam()))
                .user(userDto)
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .build();
    }
}
