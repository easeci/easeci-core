package io.easeci.core.engine.scheduler;

import io.easeci.core.engine.runtime.PipelineContext;
import io.easeci.core.node.connect.ClusterInformation;
import io.easeci.server.CommunicationType;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

import static io.easeci.api.socket.log.LogHandler.getLogPublishingHttpURI;
import static io.easeci.api.socket.log.LogHandler.getLogPublishingWsURI;

@Slf4j
class ScheduleRequestPreparer {

    private final ClusterInformation clusterInformation;

    ScheduleRequestPreparer(ClusterInformation clusterInformation) {
        this.clusterInformation = Objects.requireNonNull(clusterInformation);
    }

    ScheduleRequest prepareRequest(PipelineContext pipelineContext) {
        return ScheduleRequest.of(
                pipelineContext.getPipelineContextId(),
                encodeValue(pipelineContext.getExecutableScript()),
                ScheduleRequest.Metadata.of(
                        clusterInformation.nodeName(),
                        clusterInformation.version(),
                        clusterInformation.nodeUuid(),
                        clusterInformation.apiVersion(),
                        clusterInformation.apiVersionPrefix(),
                        clusterInformation.transferProtocol(),
                        prepareUrls()
                ),
                new ScheduleRequest.Environment(pipelineContext.getEnvironment())
        );
    }

    private ScheduleRequest.Urls prepareUrls() {
        return new ScheduleRequest.Urls(buildLogPublishingHttpURL(), buildLogPublishingWsURL());
    }

    private String encodeValue(String value) {
        return new String(Base64.getEncoder().encode(value.getBytes(StandardCharsets.UTF_8)));
    }

    private String buildLogPublishingHttpURL() {
        final CommunicationType communicationType = clusterInformation.communicationType();
        return clusterInformation.transferProtocol().name().toLowerCase()
                .concat("://")
                .concat(trimSlashes(communicationType.urlBase(clusterInformation)))
                .concat("/")
                .concat(trimSlashes(getLogPublishingHttpURI()));
    }

    private String buildLogPublishingWsURL() {
        final CommunicationType communicationType = clusterInformation.communicationType();
        return  "ws://"
                .concat(trimSlashes(communicationType.urlBase(clusterInformation)))
                .concat("/")
                .concat(trimSlashes(getLogPublishingWsURI()));
    }

    private static String trimSlashes(String value) {
        if (Objects.isNull(value) || value.isEmpty()) {
            return value;
        }
        final char SLASH = '/';
        if (value.charAt(0) == SLASH) {
            String trimmed = value.substring(1);
            return trimSlashes(trimmed);
        }
        if (value.charAt(value.length() - 1) == SLASH) {
            String trimmed = value.substring(0, value.length() - 1);
            return trimSlashes(trimmed);
        }
        return value;
    }
}
