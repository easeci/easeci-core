package io.easeci.core.workspace.projects;

import lombok.Data;

import java.nio.file.Path;
import java.util.Date;

@Data
public class PipelinePointer {
    private Long id;
    private Date createdDate;
    // reloadingDate is setting when pipeline is recreating from changed Easefile
    private Date reloadingDate;
    private String easefileOfPipeline;
    private Path easefilePath;
    private Path pipelineFilePath;
    private String name;
    private String tag;
}
