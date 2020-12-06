package io.easeci.core.workspace.projects.dto;

import lombok.Data;

@Data
public class AddProjectGroupRequest {
    private String name;
    private String tag;
    private String description;
}
