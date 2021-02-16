package io.easeci.core.engine.pipeline;

import lombok.*;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.*;

import io.easeci.core.workspace.vars.Variable;

/**
 * Pipeline class is heart of EaseCI workflow.
 * This is a POJO class that holds information about Pipeline's runtime.
 * It always must be created from Easefile (that could be obtained from various sources).
 * It is simply - bridge between text file (Easefile) and engine that executes our declarative code.
 * @author Karol Meksuła
 * 2020-11-22
 * */
@Getter
@NoArgsConstructor
public class EasefileObjectModel implements Serializable {
    private Metadata metadata;
    private Key key;
    private List<Executor> executors;
    private List<Variable> variables;
    private List<Stage> stages;
    private byte[] scriptEncoded;

    @Builder
    public EasefileObjectModel(Metadata metadata, Key key, List<Executor> executors,
                    List<Variable> variables, List<Stage> stages, byte[] scriptEncoded) {
        this.metadata = metadata;
        this.key = key;
        this.executors = executors;
        this.variables = variables;
        this.stages = stages;
        this.scriptEncoded = scriptEncoded;
    }

    @Data
    @ToString
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        EasefileObjectModel eom = (EasefileObjectModel) o;
        return Objects.equals(metadata, eom.metadata) && Objects.equals(key, eom.key) && Objects.equals(executors, eom.executors) && Objects.equals(variables, eom.variables) && Objects.equals(stages, eom.stages) && Arrays.equals(scriptEncoded, eom.scriptEncoded);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(metadata, key, executors, variables, stages);
        result = 31 * result + Arrays.hashCode(scriptEncoded);
        return result;
    }
}
