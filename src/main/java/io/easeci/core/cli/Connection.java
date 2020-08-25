package io.easeci.core.cli;

import com.google.common.net.HostAndPort;
import lombok.*;
import ratpack.http.Request;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Connection {
    private UUID connectionUuid;
    @Setter private ConnectionState connectionState;
    private String username;
    private HostAndPort clientAddress;

    public static Connection from(Request request, String username) {
        return Connection.builder()
                .connectionUuid(UUID.randomUUID())
                .username(username)
                .clientAddress(request.getRemoteAddress())
                .build();
    }

    @Override
    public String toString() {
        return "Connection{" +
                "connectionUuid=" + connectionUuid +
                ", connectionState=" + connectionState +
                ", username='" + username + '\'' +
                ", clientAddress=" + clientAddress.toString() +
                '}';
    }
}
