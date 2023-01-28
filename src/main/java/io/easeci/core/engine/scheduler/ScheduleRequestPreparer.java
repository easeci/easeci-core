package io.easeci.core.engine.scheduler;

import io.easeci.core.engine.runtime.PipelineContext;
import io.easeci.core.node.connect.ClusterInformation;
import io.easeci.server.CommunicationType;
import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

@Slf4j
class ScheduleRequestPreparer {

    private ClusterInformation clusterInformation;

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
                        buildMasterUrl()
                )
        );
    }

    String encodeValue(String value) {
        return new String(Base64.getEncoder().encode(value.getBytes(StandardCharsets.UTF_8)));
    }

    URL buildMasterUrl() {
        final CommunicationType communicationType = clusterInformation.communicationType();
        final String urlAsString = clusterInformation.transferProtocol().name().toLowerCase()
                .concat("://")
                .concat(trimSlashes(communicationType.urlBase()))
                .concat("/")
                .concat(trimSlashes(clusterInformation.apiVersionPrefix()))
                .concat("/pipeline/execution");
        try {
            return new URL(urlAsString);
        } catch (MalformedURLException e) {
            log.error("Cannot build URL address to Master Node. Worker node will must to do it itself", e);
            return null;
        }
    }

    public static String trimSlashes(String value) {
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
