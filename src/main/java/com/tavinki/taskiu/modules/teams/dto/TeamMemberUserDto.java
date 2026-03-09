package com.tavinki.taskiu.modules.teams.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamMemberUserDto {
    private String id;
    private String username;
    private String email;
    private String avatar;
}
