package io.easeci.api.node;

import io.easeci.api.ApiUtils;
import io.easeci.api.extension.ExtensionHandlers;
import io.easeci.core.extension.PluginSystemCriticalException;
import io.easeci.core.node.connect.ClusterConnectionFactory;
import io.easeci.core.node.connect.NodeConnection;
import io.easeci.core.node.connect.NodeConnectionData;
import io.easeci.server.EndpointDeclaration;
import lombok.extern.slf4j.Slf4j;
import ratpack.handling.Context;
import ratpack.http.HttpMethod;

import java.util.List;

import static io.easeci.api.validation.ApiRequestValidator.extractBody;
import static ratpack.http.MediaType.APPLICATION_JSON;

@Slf4j
public class NodeConnectionHandlers extends ExtensionHandlers {
    private final static String MAPPING = "api/v1/connection";

    public NodeConnectionHandlers() throws PluginSystemCriticalException {
    }

    @Override
    public List<EndpointDeclaration> endpoints() {
        return List.of(
                connect()
        );
    }

    private EndpointDeclaration connect() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.POST)
                .endpointUri(MAPPING)
                .handler(ctx -> extractBody(ctx, NodeConnectionRequest.class)
                        .next(request -> log.info("Node from IP address: {} is trying to connect to cluster", request.getNodeIp()))
                        .map(this::mapRequest)
                        .map(ClusterConnectionFactory::factorizeNodeConnection)
                        .mapError(throwable -> handleException(ctx, throwable))
                        .map(this::makeResponse)
                        .map(ApiUtils::write)
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    private NodeConnection handleException(Context ctx, Throwable throwable) {
        return null;
    }

    private NodeConnectionResponse makeResponse(NodeConnection nodeConnection) {
        return NodeConnectionResponse.builder()
                .nodeConnectionUuid(nodeConnection.getNodeConnectionUuid())
                .nodeConnectionState(nodeConnection.getNodeConnectionState())
                .connectionRequestOccurred(nodeConnection.getConnectionRequestOccurred())
                .lastConnectionStateChangeOccurred(nodeConnection.getLastConnectionStateChangeOccurred())
                .domainName(nodeConnection.getDomainName())
                .nodeIp(nodeConnection.getNodeIp())
                .nodePort(nodeConnection.getNodePort())
                .nodeName(nodeConnection.getNodeName())
                .transferProtocol(nodeConnection.getTransferProtocol())
                .build();
    }

    private NodeConnectionData mapRequest(NodeConnectionRequest nodeConnectionRequest) {
        return NodeConnectionData.builder()
                .nodeIp(nodeConnectionRequest.getNodeIp())
                .nodePort(nodeConnectionRequest.getNodePort())
                .nodeName(nodeConnectionRequest.getNodeName())
                .domainName(nodeConnectionRequest.getDomainName())
                .transferProtocol(nodeConnectionRequest.getTransferProtocol())
                .connectionToken(nodeConnectionRequest.getConnectionToken())
                .build();
    }
}
