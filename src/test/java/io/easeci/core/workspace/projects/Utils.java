package io.easeci.core.workspace.projects;

import io.easeci.core.engine.pipeline.Pipeline;

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
        pipelineMeta.setTag("Demo pojects example tag");
        pipelineMeta.setProjectId(INITIAL_PROJECT_ID);
        return pipelineMeta;
    }
}
