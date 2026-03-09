package com.tavinki.taskiu.modules.teams.dto;

import java.time.Instant;

import com.tavinki.taskiu.common.enums.role.TeamRole;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamMemberResponseDto {
    private String id;
    private TeamRole role;
    private TeamResponseDto team;
    private TeamMemberUserDto user;
    private Instant createdAt;
    private Instant updatedAt;
}
