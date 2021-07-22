package io.easeci.api.socket.log.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class EventRequest {
    private UUID pipelineContextId;
    private UUID workerNodeId;
    private String workerNodeHostname;

    private List<IncomingLogEntry> incomingLogEntries;
}
