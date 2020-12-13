package io.easeci.api.projects;

import io.easeci.api.communication.ApiResponse;
import io.easeci.api.projects.dto.DeleteProjectGroupRequest;
import io.easeci.api.projects.dto.DescriptionChangeProjectGroupRequest;
import io.easeci.api.projects.dto.RenameProjectGroupRequest;
import io.easeci.api.projects.dto.TagChangeProjectGroupRequest;
import io.easeci.api.validation.ApiRequestValidator;
import io.easeci.core.workspace.projects.PipelineManagementException;
import io.easeci.core.workspace.projects.ProjectGroup;
import io.easeci.core.workspace.projects.ProjectGroupIO;
import io.easeci.core.workspace.projects.ProjectManager;
import io.easeci.api.projects.dto.AddProjectGroupRequest;
import io.easeci.server.EndpointDeclaration;
import io.easeci.server.InternalHandlers;
import lombok.extern.slf4j.Slf4j;
import ratpack.handling.Context;
import ratpack.http.HttpMethod;
import ratpack.http.Status;

import java.util.List;

import static io.easeci.api.ApiUtils.write;
import static io.easeci.api.communication.ApiResponse.unknownFailure;
import static io.easeci.api.communication.ApiResponse.unknownSuccess;
import static io.easeci.api.validation.ApiRequestValidator.extractBody;
import static ratpack.http.MediaType.APPLICATION_JSON;

@Slf4j
public class ProjectGroupExtensionHandlers implements InternalHandlers {
    private final static String MAPPING = "project/";
    private ProjectGroupIO projectGroupIO;

    public ProjectGroupExtensionHandlers() {
        this.projectGroupIO = ProjectManager.getInstance();
    }

    @Override
    public List<EndpointDeclaration> endpoints() {
        return List.of(
                createProjectGroup(),
                deleteProjectGroup(),
                renameProjectGroup(),
                changeTagProjectGroup(),
                changeDescriptionProjectGroup()
        );
    }

    private EndpointDeclaration createProjectGroup() {
            return EndpointDeclaration.builder()
                    .httpMethod(HttpMethod.POST)
                    .endpointUri(MAPPING + "group/create")
                    .handler(ctx -> extractBody(ctx, AddProjectGroupRequest.class)
                                        .map(addProjectRequest -> projectGroupIO.createNewProjectGroup(addProjectRequest))
                                        .map(projectGroup -> handleCreationSuccess(ctx, projectGroup))
                                        .mapError(throwable -> handleException(ctx, throwable))
                                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                    .build();
    }

    private EndpointDeclaration deleteProjectGroup() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.POST)
                .endpointUri(MAPPING + "group/delete")
                .handler(ctx -> extractBody(ctx, DeleteProjectGroupRequest.class)
                        .map(request -> projectGroupIO.deleteProjectGroup(request.getProjectGroupId(), request.getIsHardRemoval()))
                        .map(projectGroup -> handleUpdateSuccess(ctx, projectGroup))
                        .mapError(throwable -> handleException(ctx, throwable))
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    private EndpointDeclaration renameProjectGroup() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.PUT)
                .endpointUri(MAPPING + "group/name")
                .handler(ctx -> extractBody(ctx, RenameProjectGroupRequest.class)
                        .map(request -> projectGroupIO.renameProjectGroup(request.getProjectGroupId(), request.getName()))
                        .map(projectGroup -> handleUpdateSuccess(ctx, projectGroup))
                        .mapError(throwable -> handleException(ctx, throwable))
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    private EndpointDeclaration changeTagProjectGroup() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.PUT)
                .endpointUri(MAPPING + "group/tag")
                .handler(ctx -> extractBody(ctx, TagChangeProjectGroupRequest.class)
                        .map(request -> projectGroupIO.changeTag(request.getProjectGroupId(), request.getTag()))
                        .map(projectGroup -> handleUpdateSuccess(ctx, projectGroup))
                        .mapError(throwable -> handleException(ctx, throwable))
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    private EndpointDeclaration changeDescriptionProjectGroup() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.PUT)
                .endpointUri(MAPPING + "group/description")
                .handler(ctx -> extractBody(ctx, DescriptionChangeProjectGroupRequest.class)
                        .map(request -> projectGroupIO.changeDescription(request.getProjectGroupId(), request.getDescription()))
                        .map(projectGroup -> handleDeleteSuccess(ctx, projectGroup))
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

    private byte[] handleCreationSuccess(Context ctx, ProjectGroup projectGroup) {
        ctx.getResponse().status(Status.OK);
        ApiResponse<SuccessResponse> successResponse;
        if (projectGroup != null && projectGroup.getId() != null) {
            final ProjectDomainStatus projectDomainStatus = ProjectDomainStatus.PROJECT_GROUP_CREATED;
            successResponse = ApiResponse.success(SuccessResponse.of(projectGroup.getId(), projectDomainStatus));
        } else {
            successResponse = unknownSuccess();
        }
        return write(successResponse);
    }

    private byte[] handleDeleteSuccess(Context ctx, ProjectGroup projectGroup) {
        ctx.getResponse().status(Status.OK);
        ApiResponse<SuccessResponse> apiResponse;
        if (projectGroup != null) {
            final ProjectDomainStatus projectDomainStatus = ProjectDomainStatus.PROJECT_GROUP_REMOVED;
            apiResponse = ApiResponse.success(SuccessResponse.of(projectGroup.getId(), projectDomainStatus));
        } else {
            apiResponse = unknownSuccess();
        }
        return write(apiResponse);
    }

    private byte[] handleUpdateSuccess(Context ctx, ProjectGroup projectGroup) {
        ctx.getResponse().status(Status.OK);
        ApiResponse<SuccessResponse> apiResponse;
        if (projectGroup != null) {
            final ProjectDomainStatus projectDomainStatus = ProjectDomainStatus.PROJECT_GROUP_MODIFIED;
            apiResponse = ApiResponse.success(SuccessResponse.of(projectGroup.getId(), projectDomainStatus));
        } else {
            apiResponse = unknownSuccess();
        }
        return write(apiResponse);
    }
}
