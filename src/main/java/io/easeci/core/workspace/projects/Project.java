package io.easeci.core.workspace.projects;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class Project {
    private Long id;
    private Date cratedDate;
    private Date lastModifiedDate;
    private String name;
    private String tag;
    private String description;
    private List<PipelinePointer> pipelines;
}
