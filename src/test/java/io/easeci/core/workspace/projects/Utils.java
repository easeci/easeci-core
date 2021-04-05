package io.easeci.core.workspace.projects;

import io.easeci.api.projects.dto.AddProjectGroupRequest;
import io.easeci.api.projects.dto.AddProjectRequest;
import io.easeci.core.engine.pipeline.EasefileObjectModel;
import io.easeci.core.engine.pipeline.ExecutorConfiguration;
import io.easeci.core.engine.pipeline.Key;
import io.easeci.core.engine.pipeline.Stage;
import io.easeci.core.workspace.vars.Variable;
import io.easeci.extension.command.VariableType;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import static io.easeci.core.workspace.projects.ProjectsFile.INITIAL_PROJECT_ID;

public class Utils {

    static EasefileObjectModel.Metadata preparePipelineMetadata() {
        EasefileObjectModel.Metadata pipelineMeta = new EasefileObjectModel.Metadata();
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

    static EasefileObjectModel provideEmptyPipelineForTest() {
        final EasefileObjectModel.Metadata metadata = new EasefileObjectModel.Metadata();
        metadata.setProjectId(0L);
        metadata.setPipelineId(UUID.randomUUID());
        return EasefileObjectModel.builder()
                .metadata(metadata)
                .key(Key.of(Key.KeyType.PIPELINE))
                .executorConfiguration(new ExecutorConfiguration())
                .variables(Collections.singletonList(Variable.of(VariableType.STRING, "title", "value")))
                .stages(Collections.singletonList(Stage.builder()
                        .build()))
                .scriptEncoded(new byte[0])
                .build();
    }
}
