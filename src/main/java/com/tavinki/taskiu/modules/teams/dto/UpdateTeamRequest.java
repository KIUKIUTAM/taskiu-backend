package com.tavinki.taskiu.modules.teams.dto;

import lombok.Data;

@Data
public class UpdateTeamRequest {
    private String name;
    private String description;
    private String picture;
}
