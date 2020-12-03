package io.easeci.core.workspace.projects;

import lombok.Data;

import java.nio.file.Path;
import java.util.Date;
import java.util.UUID;

@Data
public class PipelinePointer {
    private Long id;
    private Long projectId;
    private UUID pipelineId;
    private Date createdDate;
    private Path easefilePath;
    private Path pipelineFilePath;
    private String name;
    private String tag;
    private String description;
}
