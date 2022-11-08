package io.easeci.api.projects;

import io.easeci.commons.SerializeUtils;
import io.easeci.core.workspace.projects.ProjectManager;
import io.easeci.server.EndpointDeclaration;
import io.easeci.server.InternalHandlers;
import ratpack.exec.Promise;
import ratpack.http.HttpMethod;

import java.util.Collections;
import java.util.List;

import static io.easeci.api.validation.ValidationErrorResponse.unrecognizedError;
import static io.easeci.commons.SerializeUtils.write;
import static ratpack.http.MediaType.APPLICATION_JSON;

public class ProjectStructureExtensionHandlers implements InternalHandlers {
    private final static String MAPPING = "project/structure";
    private ProjectManager projectManager = ProjectManager.getInstance();

    @Override
    public List<EndpointDeclaration> endpoints() {
        return Collections.singletonList(fetchProjectStructure());
    }

    private EndpointDeclaration fetchProjectStructure() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.GET)
                .endpointUri(MAPPING)
                .handler(ctx -> Promise.value(projectManager)
                        .map(ProjectManager::getProjectGroupList)
                        .map(SerializeUtils::write)
                        .mapError(throwable -> write(unrecognizedError()))
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }
}
