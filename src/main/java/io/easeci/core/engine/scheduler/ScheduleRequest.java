package io.easeci.core.engine.scheduler;

import io.easeci.server.TransferProtocol;
import lombok.*;

import java.util.UUID;

@Value
@Getter
@AllArgsConstructor(staticName = "of")
public class ScheduleRequest {
    UUID pipelineContextId;
    String scriptEncoded;
    Metadata metadata;
    Environment environment;

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class Metadata {
        String masterNodeName;
        String masterApplicationVersion;
        UUID masterNodeUuid;
        String masterApiVersion;
        String masterApiVersionPrefix;
        TransferProtocol transferProtocol;
        Urls urls;
    }

    public record Urls(String httpLogUrl, String wsLogUrl) {
    }

    public record Environment(String name) {
    }
}
