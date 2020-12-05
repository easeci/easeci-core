package io.easeci.core.workspace.projects.dto;

import lombok.Data;

@Data
public class AddProjectRequest {
    private Long projectGroupId;
    private String name;
    private String tag;
    private String description;
}
