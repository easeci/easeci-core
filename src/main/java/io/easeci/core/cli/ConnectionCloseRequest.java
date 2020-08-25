package io.easeci.core.cli;

import lombok.Data;

import java.util.UUID;

@Data
public class ConnectionCloseRequest {
    private UUID connectionUuid;
}
