package io.easeci.core.workspace.projects;

import lombok.Getter;

public class PipelineManagementException extends RuntimeException {
    public enum PipelineManagementStatus {
        PIPELINE_ID_EXISTS,
        PIPELINE_NAME_EXISTS,
        PIPELINE_NOT_EXISTS,
        PROJECT_NOT_EXISTS,
        PROJECT_GROUP_NOT_EXISTS,
        PROJECT_ID_EXISTS,
        PROJECT_NAME_EXISTS
    }

    @Getter
    private PipelineManagementStatus status;

    public PipelineManagementException(PipelineManagementStatus status) {
        this.status = status;
    }

    @Override
    public String getMessage() {
        return "Pipeline management error: " + this.status;
    }
}
