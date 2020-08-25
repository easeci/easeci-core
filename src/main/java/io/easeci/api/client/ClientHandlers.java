package io.easeci.api.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.easeci.core.cli.ClientConnectionManager;
import io.easeci.core.cli.ConnectionCloseRequest;
import io.easeci.core.cli.ConnectionRequest;
import io.easeci.server.EndpointDeclaration;
import io.easeci.server.InternalHandlers;
import ratpack.exec.Promise;
import ratpack.http.TypedData;

import java.util.List;

import static ratpack.http.HttpMethod.*;
import static ratpack.http.MediaType.APPLICATION_JSON;

public class ClientHandlers implements InternalHandlers {
    private final static String MAPPING = "client/";
    private ClientConnectionManager clientConnectionManager;
    private ObjectMapper objectMapper;

    public ClientHandlers() {
        this.clientConnectionManager = ClientConnectionManager.getInstance();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<EndpointDeclaration> endpoints() {
        return List.of(establishConnection(), getConnections(), closeConnection());
    }

    private EndpointDeclaration establishConnection() {
        return EndpointDeclaration.builder()
                .httpMethod(POST)
                .endpointUri(MAPPING + "connection/open")
                .handler(ctx -> Promise.value(ctx.getRequest())
                        .map(request -> clientConnectionManager.initConnection(
                                request,
                                request.getBody()
                                        .map(TypedData::getBytes)
                                        .map(bytes -> objectMapper.readValue(bytes, ConnectionRequest.class))
                                ).map(connectionStateResponse -> objectMapper.writeValueAsBytes(connectionStateResponse))
                        ).then(promise -> promise.then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes))))
                .build();
    }

    private EndpointDeclaration getConnections() {
        return EndpointDeclaration.builder()
                .httpMethod(GET)
                .endpointUri(MAPPING + "connections")
                .handler(ctx -> Promise.value(clientConnectionManager.getConnections())
                        .map(connections -> objectMapper.writeValueAsBytes(connections))
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    private EndpointDeclaration closeConnection() {
        return EndpointDeclaration.builder()
                .httpMethod(POST)
                .endpointUri(MAPPING + "connection/close")
                .handler(ctx -> ctx.getRequest().getBody()
                        .map(TypedData::getBytes)
                        .map(bytes -> objectMapper.readValue(bytes, ConnectionCloseRequest.class))
                        .map(connectionCloseRequest -> clientConnectionManager.closeConnection(connectionCloseRequest))
                        .map(connectionDto -> objectMapper.writeValueAsBytes(connectionDto))
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }
}
