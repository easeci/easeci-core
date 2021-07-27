package io.easeci.core.engine.runtime.commons;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

public enum PipelineRunStatus {
    PIPELINE_EXEC_QUEUED {
        @Override
        public String getMessage() {
            return "Pipeline was put on queue and it will be run";
        }
    },
    PIPELINE_EXEC_STARTED {
        @Override
        public String getMessage() {
            return "Pipeline was run with success and now it is processing";
        }
    },
    PIPELINE_EXEC_FAILED {
        @Override
        public String getMessage() {
            return "Pipeline was not run";
        }
    },
    PIPELINE_NOT_FOUND {
        @Override
        public String getMessage() {
            return "Pipeline was not found on this EaseCI instance";
        }
    };

    public abstract String getMessage();

    @Data
    @AllArgsConstructor(staticName = "of")
    public static class PipelineRunStatusWrapper {
        private PipelineRunStatus pipelineRunStatus;
        private UUID pipelineContextId;
    }
}
