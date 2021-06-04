package io.easeci.api.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.easeci.api.log.dto.EventRequest;
import io.easeci.core.engine.runtime.PipelineContextSystem;
import io.easeci.core.engine.runtime.logs.LogEntry;
import io.easeci.core.engine.runtime.logs.LogRail;
import io.easeci.core.log.ApplicationLevelLog;
import io.easeci.core.output.Event;
import io.easeci.core.output.EventType;
import io.easeci.server.EndpointDeclaration;
import io.easeci.server.InternalHandlers;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static ratpack.http.HttpMethod.POST;
import static ratpack.http.MediaType.APPLICATION_JSON;

public class LogHandler implements InternalHandlers {

    @Override
    public List<EndpointDeclaration> endpoints() {
        return List.of(
                addEvent(),
                publishWebsocketTest()
        );
    }

    private EndpointDeclaration addEvent() {
        return EndpointDeclaration.builder()
                .httpMethod(POST)
                .endpointUri("api/v1/log")
                .handler(ctx -> ctx.getRequest().getBody()
                        .map(typedData -> new ObjectMapper().readValue(typedData.getBytes(), EventRequest.class))
                        .next(request -> ApplicationLevelLog.getInstance()
                                .handle(Event.builder()
                                        .eventMeta(Event.EventMeta.builder()
                                                .publishedBy("API request")
                                                .title(request.getTitle())
                                                .publishTimestamp(LocalDateTime.now())
                                                .eventType(EventType.API)
                                                .build())
                                        .content(request.getContent())
                                        .build()))
                        .then(request -> ctx.getResponse()
                                .contentType(APPLICATION_JSON)
                                .status(HttpResponseStatus.OK.code())
                                .send()))
                .build();
    }

    private EndpointDeclaration publishWebsocketTest() {
        return EndpointDeclaration.builder()
                .httpMethod(POST)
                .endpointUri("api/v1/ws/test")
                .handler(ctx -> ctx.getRequest().getBody()
                        .map(typedData -> new ObjectMapper().readValue(typedData.getBytes(), EventRequest.class))
                        .next(request -> {
                            UUID pipelineContextId = UUID.fromString(request.getTitle());
                            LogRail logRail = PipelineContextSystem.getInstance().getLogRail(pipelineContextId);
                            logRail.publish(LogEntry.builder()
                                    .author("easeci-core-master")
                                    .header("[INFO]")
                                    .createdDateTime(LocalDateTime.now())
                                    .text(request.getContent() + ", " + request.getTitle())
                                    .build());
                        })
                        .then(request -> ctx.getResponse()
                                .contentType(APPLICATION_JSON)
                                .status(HttpResponseStatus.OK.code())
                                .send()))
                .build();
    }
}
