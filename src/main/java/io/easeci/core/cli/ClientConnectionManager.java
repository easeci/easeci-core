package io.easeci.core.cli;

import io.easeci.commons.YamlUtils;
import io.easeci.core.node.NodeUtils;
import io.vavr.Tuple2;
import lombok.extern.slf4j.Slf4j;
import ratpack.exec.Promise;
import ratpack.http.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.easeci.core.cli.ConnectionState.*;
import static io.easeci.core.log.ApplicationLevelLogFacade.note;
import static io.easeci.core.workspace.LocationUtils.getGeneralYmlLocation;
import static java.util.Objects.isNull;

@Slf4j
public class ClientConnectionManager {
    private static ClientConnectionManager instance;
    private List<Connection> connections;
    private static int MAX_CONNECTION_BY_HOST;

    private ClientConnectionManager() {
        this.connections = new ArrayList<>(0);
        Map<?, ?> yamlValues = YamlUtils.ymlLoad(getGeneralYmlLocation());
        MAX_CONNECTION_BY_HOST = (Integer) YamlUtils.ymlGet(yamlValues, "connection.max-by-host").getValue();
    }

    public static ClientConnectionManager getInstance() {
        if (isNull(instance)) {
            instance = new ClientConnectionManager();
            return instance;
        }
        return instance;
    }

    public Promise<ConnectionStateResponse> initConnection(Request request, Promise<ConnectionRequest> requestBody) {
        return requestBody.map(connectionRequest -> {
            note("EaseCI node connection attempt occurred",
                    "Attempt for connect to EaseCI node from IP: "
                            + request.getRemoteAddress().getHost()
                            + " with user: " + connectionRequest.getUsername());
            ConnectionState state = ESTABLISHED;
            int connections = countConnectionsByUsername(connectionRequest.getUsername());
            if (connections >= MAX_CONNECTION_BY_HOST) {
                log.info("=====> Connection limit for username: {}", connectionRequest.getUsername());
                state = CONNECTIONS_LIMIT;
            }
            return new Tuple2<>(connectionRequest.getUsername(), state);
        }).map(tuple2 -> {
            Connection connection = Connection.from(request, tuple2._1);
            connection.setConnectionState(tuple2._2);
            if (connection.getConnectionState().equals(ESTABLISHED)) {
                this.connections.add(connection);
            }
            note("EaseCI node connection attempt was rejected",
                    "Attempt for connect to EaseCI node from IP: " +
                            request.getRemoteAddress().getHost() +
                            " with user: " + connection.getUsername() +
                            " was rejected with error code: " + connection.getConnectionState().name());
            return connection;
        }).next(connection -> log.info("=====> Remote connection established: {}", connection.toString()))
                .map(connection -> {
                    ConnectionStateResponse response = ConnectionStateResponse.of(
                            NodeUtils.nodeName(),
                            connection.getConnectionUuid(),
                            connection.getUsername(),
                            connection.getConnectionState()
                    );
                    connection.setConnectionState(ALIVE);
                    note("EaseCI node new connection established",
                            "Successfully connected to EaseCI node from IP: " + connection.getClientAddress().getHost() + " with user: " + connection.getUsername());
                    return response;
                });
    }

    private int countConnectionsByUsername(String username) {
        return Math.toIntExact(connections.stream()
                .filter(connection -> connection.getUsername().equals(username))
                .count());
    }

    public ConnectionDto closeConnection(ConnectionCloseRequest request) {
        return ConnectionDto.from(connections.stream()
                .filter(connection -> connection.getConnectionUuid().equals(request.getConnectionUuid()))
                .findFirst()
                .map(connection -> {
                    this.connections.remove(connection);
                    connection.setConnectionState(CLOSED);
                    note("EaseCI node connection closed", "Connection to EaseCI node for IP: "
                            + connection.getClientAddress().getHost()
                            + ", for user: " + connection.getUsername()
                            + " is just closed for now");
                    return connection;
                }).orElseThrow(() -> new IllegalStateException("Connection is closed now or not exists"))
        );
    }

    public List<ConnectionDto> getConnections() {
        return this.connections.stream()
                .map(ConnectionDto::from)
                .collect(Collectors.toList());
    }
}
