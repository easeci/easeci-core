package io.easeci.api.runtime;

import io.easeci.commons.SerializeUtils;
import io.easeci.core.engine.runtime.PipelineContextSystem;
import io.easeci.core.engine.runtime.PipelineRunEntryPoint;
import io.easeci.core.engine.runtime.commons.PipelineContextState;
import io.easeci.core.engine.runtime.commons.PipelineRunStatus;
import io.easeci.server.EndpointDeclaration;
import io.easeci.server.InternalHandlers;
import lombok.extern.slf4j.Slf4j;
import ratpack.exec.Promise;
import ratpack.handling.Context;
import ratpack.http.HttpMethod;
import ratpack.http.Status;

import java.util.List;

import static io.easeci.api.validation.ApiRequestValidator.extractBody;
import static io.easeci.core.engine.runtime.commons.PipelineRunStatus.PIPELINE_EXEC_FAILED;
import static ratpack.http.MediaType.APPLICATION_JSON;

@Slf4j
public class RuntimeHandlers implements InternalHandlers {

    private final static String API_PREFIX = "api/v1/";
    private final static String MAPPING = "pipeline/runtime";
    private final PipelineRunEntryPoint entryPoint;

    public RuntimeHandlers() {
        this.entryPoint = PipelineContextSystem.getInstance();
    }

    @Override
    public List<EndpointDeclaration> endpoints() {
        return List.of(
                runPipeline(),
                getPipelineContextList()
        );
    }

    private EndpointDeclaration runPipeline() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.POST)
                .endpointUri(MAPPING + "/run")
                .handler(ctx -> extractBody(ctx, RunPipelineRequest.class)
                        .next(runPipelineRequest -> log.info("Request to start pipeline runtime occurred for pipelineId: {}", runPipelineRequest.getPipelineId()))
                        .map(runPipelineRequest -> entryPoint.runPipeline(runPipelineRequest.getPipelineId()))
                        .map(pipelineRunStatus -> handleSuccess(ctx, pipelineRunStatus))
                        .mapError(throwable -> handleException(ctx, throwable))
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes))
                )
                .build();
    }

    private EndpointDeclaration getPipelineContextList() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.GET)
                .endpointUri(API_PREFIX + MAPPING + "/list")
                .handler(ctx -> Promise.value(ctx.getRequest())
                        .next(request -> log.info("Request to fetch current pipeline contexts runtime"))
                        .map(request -> entryPoint.contextQueueState())
                        .map(contextList -> handleSuccess(ctx, contextList))
                        .mapError(throwable -> handleException(ctx, throwable))
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes))
                )
                .build();
    }

    private byte[] handleException(Context ctx, Throwable throwable) {
        throwable.printStackTrace();
        ctx.getResponse().status(Status.BAD_REQUEST);
        RunPipelineResponse response = RunPipelineResponse.of(PipelineRunStatus.PipelineRunStatusWrapper.of(PIPELINE_EXEC_FAILED, null),
                                                              PIPELINE_EXEC_FAILED.getMessage(), throwable);
        return SerializeUtils.write(response);
    }

    private byte[] handleSuccess(Context ctx, PipelineRunStatus.PipelineRunStatusWrapper pipelineRunStatusWrapper) {
        log.info("Successfully processed run pipeline request");
        ctx.getResponse().status(Status.OK);
        RunPipelineResponse response = RunPipelineResponse.of(pipelineRunStatusWrapper, pipelineRunStatusWrapper.getPipelineRunStatus().getMessage(), null);
        return SerializeUtils.write(response);
    }

    private byte[] handleSuccess(Context ctx, List<PipelineContextState> pipelineContextStates) {
        log.info("Fetched state of pipeline context runtimes");
        ctx.getResponse().status(Status.OK);
        return SerializeUtils.write(pipelineContextStates);
    }
}
