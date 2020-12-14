package io.easeci.core.engine.pipeline;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
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
public class Pipeline implements Serializable {
    private Metadata metadata;
    private Key key;
    private List<Executor> executors;
    private List<Variable> variables;
    private List<Stage> stages;
    private byte[] scriptEncoded;

    @Builder
    public Pipeline(Metadata metadata, Key key, List<Executor> executors,
                    List<Variable> variables, List<Stage> stages, byte[] scriptEncoded) {
        this.metadata = metadata;
        this.key = key;
        this.executors = executors;
        this.variables = variables;
        this.stages = stages;
        this.scriptEncoded = scriptEncoded;
    }

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
        private String description;
    }
}
