package io.easeci.core.engine.pipeline;

import lombok.Data;
import lombok.Getter;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Pipeline class is heart of EaseCI workflow.
 * This is a POJO class that holds information about Pipeline's runtime.
 * It always must be created from Easefile (that could be obtained from various sources).
 * It is simply - bridge between text file (Easefile) and engine that executes our declarative code.
 * @author Karol Meksuła
 * 2020-11-22
 * */
@Getter
public class Pipeline {
    private Metadata metadata;
    private List<PipeDataSet> pipeDataSets;

    @Data
    public static class Metadata {
        private Long projectId;
        private UUID pipelineId;
        private Date createdDate;
        private Path easefilePath;
        private Date lastReparseDate;
        private String name;
        private Path pipelineFilePath;
        private String tag;
    }

    // here all elements of Easefile specification like variables, directives, etc.
    static class PipeDataSet {

    }
}
