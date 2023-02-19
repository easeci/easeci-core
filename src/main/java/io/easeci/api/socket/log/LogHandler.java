package io.easeci.api.socket.log;

import io.easeci.api.socket.log.dto.EventRequest;
import io.easeci.commons.SerializeUtils;
import io.easeci.core.engine.runtime.PipelineContextSystem;
import io.easeci.core.engine.runtime.logs.LogEntry;
import io.easeci.core.engine.runtime.logs.LogRail;
import io.easeci.core.node.connect.ClusterInformation;
import io.easeci.core.node.connect.ClusterInformationDefault;
import io.easeci.server.EndpointDeclaration;
import io.easeci.server.InternalHandlers;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static ratpack.http.HttpMethod.POST;
import static ratpack.http.MediaType.APPLICATION_JSON;

@Slf4j
public class LogHandler implements InternalHandlers {

    private static final String LOG_PUBLISHING_HTTP = "/pipeline/logs/http";
    private static final String LOG_PUBLISHING_WS = "/pipeline/logs/ws";

    private static final ClusterInformation clusterInformation = new ClusterInformationDefault();

    @Override
    public List<EndpointDeclaration> endpoints() {
        return List.of(
                publishLogs()
        );
    }

    private EndpointDeclaration publishLogs() {
        return EndpointDeclaration.builder()
                .httpMethod(POST)
                .endpointUri(getLogPublishingHttpURI().substring(1))
                .handler(ctx -> ctx.getRequest().getBody()
                        .map(typedData -> SerializeUtils.read(typedData.getBytes(), EventRequest.class).orElseThrow())
                        .next(eventRequest -> {
                            log.info("Logs handled from worker node: {}", eventRequest.getWorkerNodeId());
                            final LogRail logRail = PipelineContextSystem.getInstance()
                                                                         .getLogRail(eventRequest.getPipelineContextId());
                            eventRequest.getIncomingLogEntries()
                                        .forEach(incomingLogEntry ->
                                            logRail.publish(LogEntry.builder()
                                            .author(eventRequest.getWorkerNodeHostname())
                                            .header(incomingLogEntry.getHeader())
                                            .timestamp(incomingLogEntry.getTimestamp())
                                            .text(incomingLogEntry.getTitle() + ", " + incomingLogEntry.getContent())
                                            .build()));
                        }).then(request -> ctx.getResponse()
                                              .contentType(APPLICATION_JSON)
                                              .status(HttpResponseStatus.OK.code())
                                              .send()))
                .build();
    }

    public static String getLogPublishingHttpURI() {
        return clusterInformation.apiVersionPrefix() + LOG_PUBLISHING_HTTP;
    }

    public static String getLogPublishingWsURI() {
        return clusterInformation.apiVersionPrefix() + LOG_PUBLISHING_WS;
    }
}
