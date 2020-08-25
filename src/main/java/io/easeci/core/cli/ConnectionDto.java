package io.easeci.core.cli;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConnectionDto {
    private UUID connectionUuid;
    private ConnectionState connectionState;
    private String username;
    private String host;

    public static ConnectionDto from(Connection connection) {
        return new ConnectionDto(
                connection.getConnectionUuid(),
                connection.getConnectionState(),
                connection.getUsername(),
                connection.getClientAddress().getHost()
        );
    }
}
