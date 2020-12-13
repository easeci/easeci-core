package io.easeci.api.projects;

import io.easeci.api.communication.ApiResponse;
import io.easeci.api.projects.dto.*;
import io.easeci.api.validation.ApiRequestValidator;
import io.easeci.core.workspace.projects.*;
import io.easeci.server.EndpointDeclaration;
import io.easeci.server.InternalHandlers;
import ratpack.handling.Context;
import ratpack.http.HttpMethod;
import ratpack.http.Status;

import java.util.List;

import static io.easeci.api.ApiUtils.write;
import static io.easeci.api.communication.ApiResponse.unknownFailure;
import static io.easeci.api.communication.ApiResponse.unknownSuccess;
import static io.easeci.api.validation.ApiRequestValidator.extractBody;
import static ratpack.http.MediaType.APPLICATION_JSON;

public class PipelinePointerExtensionHandlers implements InternalHandlers {
    private final static String MAPPING = "pipeline/pointer";
    private PipelinePointerIO pipelinePointerIO;

    public PipelinePointerExtensionHandlers() {
        this.pipelinePointerIO = ProjectManager.getInstance();
    }

    @Override
    public List<EndpointDeclaration> endpoints() {
        return List.of(
                deletePipelinePointer(),
                renamePipelinePointer(),
                changeTagPipelinePointer(),
                changeDescriptionPipelinePointer()
        );
    }

    private EndpointDeclaration deletePipelinePointer() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.POST)
                .endpointUri(MAPPING + "delete")
                .handler(ctx -> extractBody(ctx, DeletePipelinePointerRequest.class)
                        .map(request -> pipelinePointerIO.deletePipelinePointer(request.getProjectId(), request.getPipelinePointerId()))
                        .map(project -> handleUpdateSuccess(ctx, project))
                        .mapError(throwable -> handleException(ctx, throwable))
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    private EndpointDeclaration renamePipelinePointer() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.PUT)
                .endpointUri(MAPPING + "rename")
                .handler(ctx -> extractBody(ctx, RenamePipelinePointerRequest.class)
                        .map(request -> pipelinePointerIO.renamePipelinePointer(request.getProjectId(), request.getPipelinePointerId(), request.getName()))
                        .map(project -> handleUpdateSuccess(ctx, project))
                        .mapError(throwable -> handleException(ctx, throwable))
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    private EndpointDeclaration changeTagPipelinePointer() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.PUT)
                .endpointUri(MAPPING + "tag")
                .handler(ctx -> extractBody(ctx, TagChangePipelinePointerRequest.class)
                        .map(request -> pipelinePointerIO.changePipelinePointerTag(request.getProjectId(), request.getPipelinePointerId(), request.getTag()))
                        .map(project -> handleUpdateSuccess(ctx, project))
                        .mapError(throwable -> handleException(ctx, throwable))
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    private EndpointDeclaration changeDescriptionPipelinePointer() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.PUT)
                .endpointUri(MAPPING + "description")
                .handler(ctx -> extractBody(ctx, DescriptionChangePipelinePointerRequest.class)
                        .map(request -> pipelinePointerIO.changePipelinePointerDescription(request.getProjectId(), request.getPipelinePointerId(), request.getDescription()))
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

    private byte[] handleDeleteSuccess(Context ctx, PipelinePointer pipelinePointer) {
        ctx.getResponse().status(Status.OK);
        ApiResponse<SuccessResponse> apiResponse;
        if (pipelinePointer != null) {
            final ProjectDomainStatus projectDomainStatus = ProjectDomainStatus.PIPELINE_POINTER_REMOVED;
            apiResponse = ApiResponse.success(SuccessResponse.of(pipelinePointer.getId(), projectDomainStatus));
        } else {
            apiResponse = unknownSuccess();
        }
        return write(apiResponse);
    }

    private byte[] handleUpdateSuccess(Context ctx, PipelinePointer pipelinePointer) {
        ctx.getResponse().status(Status.OK);
        ApiResponse<SuccessResponse> apiResponse;
        if (pipelinePointer != null) {
            final ProjectDomainStatus projectDomainStatus = ProjectDomainStatus.PIPELINE_POINTER_MODIFIED;
            apiResponse = ApiResponse.success(SuccessResponse.of(pipelinePointer.getId(), projectDomainStatus));
        } else {
            apiResponse = unknownSuccess();
        }
        return write(apiResponse);
    }
}
