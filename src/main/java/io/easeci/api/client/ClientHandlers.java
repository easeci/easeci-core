package io.easeci.api.client;

import io.easeci.api.validation.ApiRequestValidator;
import io.easeci.commons.SerializeUtils;
import io.easeci.core.cli.ClientConnectionManager;
import io.easeci.core.cli.ConnectionCloseRequest;
import io.easeci.core.cli.ConnectionDto;
import io.easeci.core.cli.ConnectionRequest;
import io.easeci.server.EndpointDeclaration;
import io.easeci.server.InternalHandlers;
import ratpack.exec.Promise;

import java.util.List;

import static ratpack.http.HttpMethod.*;
import static ratpack.http.MediaType.APPLICATION_JSON;

public class ClientHandlers implements InternalHandlers {
    private final static String MAPPING = "client/";
    private ClientConnectionManager clientConnectionManager;

    public ClientHandlers() {
        this.clientConnectionManager = ClientConnectionManager.getInstance();
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
                        .map(request -> clientConnectionManager.initConnection(request, ApiRequestValidator.extractBody(request, ConnectionRequest.class))
                                .map(SerializeUtils::write)
                                .mapError(ApiRequestValidator::handleException))
                        .mapError(ApiRequestValidator::handleExceptionPromise)
                        .then(promise -> promise.then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes))))
                .build();
    }

    private EndpointDeclaration getConnections() {
        return EndpointDeclaration.builder()
                .httpMethod(GET)
                .endpointUri(MAPPING + "connections")
                .handler(ctx -> Promise.value(clientConnectionManager.getConnections())
                        .map(SerializeUtils::write)
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    private EndpointDeclaration closeConnection() {
        return EndpointDeclaration.builder()
                .httpMethod(POST)
                .endpointUri(MAPPING + "connection/close")
                .handler(ctx -> ApiRequestValidator.extractBody(ctx.getRequest(), ConnectionCloseRequest.class)
                        .map(connectionCloseRequest -> clientConnectionManager.closeConnection(connectionCloseRequest))
                        .mapError(throwable -> ConnectionDto.notExists())
                        .map(SerializeUtils::write)
                        .mapError(ApiRequestValidator::handleException)
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }
}
