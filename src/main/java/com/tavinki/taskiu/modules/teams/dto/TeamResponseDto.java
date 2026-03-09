package com.tavinki.taskiu.modules.teams.dto;


import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamResponseDto {
    private String id;
    private String teamId;
    private String teamName;
    private String teamDescription;
    private String teamPicture;
    private Instant createdAt;
    private Instant updatedAt;
}
