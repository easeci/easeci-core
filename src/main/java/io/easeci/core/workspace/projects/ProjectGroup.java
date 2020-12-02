package io.easeci.core.workspace.projects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectGroup {
    private Long id;
    private Date cratedDate;
    private Date lastModifiedDate;
    private String name;
    private String tag;
    private String description;
    private List<Project> projects;
}
