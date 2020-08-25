package io.easeci.core.cli;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor(staticName = "of")
public class ConnectionStateResponse {
    private String nodeName;
    private UUID connectionUuid;
    private String username;
    private ConnectionState connectionState;
}
