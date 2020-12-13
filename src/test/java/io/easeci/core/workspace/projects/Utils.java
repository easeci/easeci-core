package io.easeci.core.workspace.projects;

import io.easeci.core.engine.pipeline.Pipeline;
import io.easeci.api.projects.dto.AddProjectGroupRequest;
import io.easeci.api.projects.dto.AddProjectRequest;

import java.nio.file.Paths;
import java.util.Date;
import java.util.UUID;

import static io.easeci.core.workspace.projects.ProjectsFile.INITIAL_PROJECT_ID;

public class Utils {

    static Pipeline.Metadata preparePipelineMetadata() {
        Pipeline.Metadata pipelineMeta = new Pipeline.Metadata();
        pipelineMeta.setPipelineId(UUID.randomUUID());
        pipelineMeta.setCreatedDate(new Date());
        pipelineMeta.setEasefilePath(Paths.get("/tmp/Easefile"));
        pipelineMeta.setName("Demo pipeline");
        pipelineMeta.setPipelineFilePath(Paths.get("/tmp/workspace/projects/pipelines"));
        pipelineMeta.setTag("Demo projects example tag");
        pipelineMeta.setProjectId(INITIAL_PROJECT_ID);
        return pipelineMeta;
    }

    static AddProjectRequest prepareAddProjectRequest(Long projectGroupId) {
        AddProjectRequest request = new AddProjectRequest();
        request.setProjectGroupId(projectGroupId);
        request.setName("Demo project");
        request.setTag("Demo projects example tag");
        request.setDescription("Example project, created in testing purposes");
        return request;
    }

    static AddProjectGroupRequest prepareAddProjectGroupRequest() {
        AddProjectGroupRequest request = new AddProjectGroupRequest();
        request.setName("Demo project group");
        request.setTag("Demo projects example tag");
        request.setDescription("Example project group");
        return request;
    }
}
