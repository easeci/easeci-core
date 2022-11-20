package io.easeci.core.engine.scheduler;

import io.easeci.server.TransferProtocol;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

import java.net.URL;
import java.util.UUID;

@Value
@Getter
@AllArgsConstructor(staticName = "of")
public class ScheduleRequest {
    UUID pipelineContextId;
    String scriptEncoded;
    Metadata metadata;

    @AllArgsConstructor(staticName = "of")
    public static class Metadata {
        String masterNodeName;
        String masterApplicationVersion;
        UUID masterNodeUuid;
        String masterApiVersion;
        String masterApiVersionPrefix;
        TransferProtocol transferProtocol;
        URL logUrl;
    }
}