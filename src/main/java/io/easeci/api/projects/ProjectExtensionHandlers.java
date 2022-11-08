package io.easeci.api.projects;

import io.easeci.api.communication.ApiResponse;
import io.easeci.api.projects.dto.*;
import io.easeci.api.validation.ApiRequestValidator;
import io.easeci.core.workspace.projects.*;
import io.easeci.api.projects.dto.AddProjectRequest;
import io.easeci.server.EndpointDeclaration;
import io.easeci.server.InternalHandlers;
import ratpack.handling.Context;
import ratpack.http.HttpMethod;
import ratpack.http.Status;

import java.util.List;

import static io.easeci.api.communication.ApiResponse.unknownFailure;
import static io.easeci.api.communication.ApiResponse.unknownSuccess;
import static io.easeci.api.validation.ApiRequestValidator.extractBody;
import static io.easeci.commons.SerializeUtils.write;
import static ratpack.http.MediaType.APPLICATION_JSON;

public class ProjectExtensionHandlers implements InternalHandlers {
    private final static String MAPPING = "project/";
    private ProjectIO projectIO;

    public ProjectExtensionHandlers() {
        this.projectIO = ProjectManager.getInstance();
    }

    @Override
    public List<EndpointDeclaration> endpoints() {
        return List.of(
                createProject(),
                deleteProject(),
                renameProject(),
                changeTagProject(),
                changeDescriptionProject()
        );
    }

    private EndpointDeclaration createProject() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.POST)
                .endpointUri(MAPPING + "create")
                .handler(ctx -> extractBody(ctx, AddProjectRequest.class)
                        .map(addProjectRequest -> projectIO.createNewProject(addProjectRequest))
                        .map(project -> handleCreationSuccess(ctx, project))
                        .mapError(throwable -> handleException(ctx, throwable))
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    private EndpointDeclaration deleteProject() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.POST)
                .endpointUri(MAPPING + "delete")
                .handler(ctx -> extractBody(ctx, DeleteProjectRequest.class)
                        .map(request -> projectIO.deleteProject(request.getProjectGroupId(), request.getProjectId(), request.getIsHardRemoval()))
                        .map(project -> handleUpdateSuccess(ctx, project))
                        .mapError(throwable -> handleException(ctx, throwable))
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    private EndpointDeclaration renameProject() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.PUT)
                .endpointUri(MAPPING + "rename")
                .handler(ctx -> extractBody(ctx, RenameProjectRequest.class)
                        .map(request -> projectIO.renameProject(request.getProjectId(), request.getName()))
                        .map(project -> handleUpdateSuccess(ctx, project))
                        .mapError(throwable -> handleException(ctx, throwable))
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    private EndpointDeclaration changeTagProject() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.PUT)
                .endpointUri(MAPPING + "tag")
                .handler(ctx -> extractBody(ctx, TagChangeProjectRequest.class)
                        .map(request -> projectIO.changeProjectTag(request.getProjectId(), request.getTag()))
                        .map(project -> handleUpdateSuccess(ctx, project))
                        .mapError(throwable -> handleException(ctx, throwable))
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    private EndpointDeclaration changeDescriptionProject() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.PUT)
                .endpointUri(MAPPING + "description")
                .handler(ctx -> extractBody(ctx, DescriptionChangeProjectRequest.class)
                        .map(request -> projectIO.changeProjectDescription(request.getProjectId(), request.getDescription()))
                        .map(project -> handleDeleteSuccess(ctx, project))
                        .mapError(throwable -> handleException(ctx, throwable))
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    private byte[] handleException(Context ctx, Throwable throwable) {
        ApiResponse<?> apiResponse;
        if (throwable instanceof ApiRequestValidator.ValidationErrorSignal) {
            ApiRequestValidator.ValidationErrorSignal signal = (ApiRequestValidator.ValidationErrorSignal) throwable;
            ctx.getResponse().status(Status.BAD_REQUEST);
            return signal.getResponse();
        }
        if (throwable instanceof PipelineManagementException) {
            PipelineManagementException pipelineManagementException = (PipelineManagementException) throwable;
            final String domainStatus = pipelineManagementException.getStatus().name();
            final String message = pipelineManagementException.getMessage();
            apiResponse = ApiResponse.failure(domainStatus, message);
        } else {
            apiResponse = unknownFailure();
        }
        return write(apiResponse);
    }

    private byte[] handleCreationSuccess(Context ctx, Project project) {
        ctx.getResponse().status(Status.OK);
        ApiResponse<SuccessResponse> successResponse;
        if (project != null && project.getId() != null) {
            final ProjectDomainStatus projectDomainStatus = ProjectDomainStatus.PROJECT_CREATED;
            successResponse = ApiResponse.success(SuccessResponse.of(project.getId(), projectDomainStatus));
        } else {
            successResponse = unknownSuccess();
        }
        return write(successResponse);
    }

    private byte[] handleDeleteSuccess(Context ctx, Project project) {
        ctx.getResponse().status(Status.OK);
        ApiResponse<SuccessResponse> apiResponse;
        if (project != null) {
            final ProjectDomainStatus projectDomainStatus = ProjectDomainStatus.PROJECT_REMOVED;
            apiResponse = ApiResponse.success(SuccessResponse.of(project.getId(), projectDomainStatus));
        } else {
            apiResponse = unknownSuccess();
        }
        return write(apiResponse);
    }

    private byte[] handleUpdateSuccess(Context ctx, Project project) {
        ctx.getResponse().status(Status.OK);
        ApiResponse<SuccessResponse> apiResponse;
        if (project != null) {
            final ProjectDomainStatus projectDomainStatus = ProjectDomainStatus.PROJECT_MODIFIED;
            apiResponse = ApiResponse.success(SuccessResponse.of(project.getId(), projectDomainStatus));
        } else {
            apiResponse = unknownSuccess();
        }
        return write(apiResponse);
    }
}
