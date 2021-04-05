package io.easeci.core.engine.runtime;

public enum PipelineRunStatus {
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
    };

    public abstract String getMessage();
}
