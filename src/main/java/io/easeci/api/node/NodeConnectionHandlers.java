package io.easeci.api.node;

import io.easeci.api.Errorable;
import io.easeci.api.extension.ExtensionHandlers;
import io.easeci.api.validation.ApiRequestValidator;
import io.easeci.core.extension.PluginSystemCriticalException;
import io.easeci.core.node.connect.*;
import io.easeci.commons.SerializeUtils;
import io.easeci.core.workspace.WorkspaceInitializationException;
import io.easeci.server.EndpointDeclaration;
import lombok.extern.slf4j.Slf4j;
import ratpack.exec.Promise;

import java.util.List;

import static io.easeci.api.validation.ApiRequestValidator.extractBody;
import static ratpack.http.MediaType.APPLICATION_JSON;

@Slf4j
public class NodeConnectionHandlers extends ExtensionHandlers {
    private final static String MAPPING = "api/v1/connection";
    private final ClusterConnectionHub clusterConnectionHub = ClusterConnectionHub.getInstance();

    public NodeConnectionHandlers() throws PluginSystemCriticalException, WorkspaceInitializationException {
    }

    @Override
    public List<EndpointDeclaration> endpoints() {
        return List.of(
                getConnectAndClusterDetails()
        );
    }

    private EndpointDeclaration getConnectAndClusterDetails() {
        return EndpointDeclaration.builder()
                                  .multiEndpointDeclaration(true)
                                  .endpointUri(MAPPING)
                                  .handler(ctx -> ctx.byMethod(byMethodSpec -> byMethodSpec.get(localContext -> Promise.value(clusterConnectionHub)
                                                                                                                   .next(hub -> log.info("Fetching cluster details from server"))
                                                                                                                   .map(ClusterConnectionHub::getClusterDetails)
                                                                                                                   .map(SerializeUtils::write)
                                                                                                                   .mapError(this::handleGetClusterDetailsException)
                                                                                                                   .then(bytes -> localContext.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                                                                                       .post(localContext -> extractBody(ctx, NodeConnectionRequest.class)
                                                                                                                   .next(request -> log.info("Node from IP address: {} is trying to connect to cluster", request.getNodeIp()))
                                                                                                                   .map(this::mapRequest)
                                                                                                                   .map(ClusterConnectionFactory::factorizeNodeConnection)
                                                                                                                   .map(this::makeResponse)
                                                                                                                   .map(SerializeUtils::write)
                                                                                                                   .mapError(this::handleConnectionException)
                                                                                                                   .then(bytes -> localContext.getResponse().contentType(APPLICATION_JSON).send(bytes)))))
                                  .build();
    }

    private byte[] handleConnectionException(Throwable throwable) {
        if (throwable instanceof ApiRequestValidator.ValidationErrorSignal) {
            ApiRequestValidator.ValidationErrorSignal validationErrorSignal = (ApiRequestValidator.ValidationErrorSignal) throwable;
            return validationErrorSignal.getResponse();
        }
        return SerializeUtils.write(NodeConnectionResponse.builder()
                                                    .nodeConnectionState(NodeConnectionState.CONNECTION_ERROR)
                                                    .build());
    }

    private byte[] handleGetClusterDetailsException(Throwable throwable) {
        log.error("Exception handled while hit GET /{} endpoint: {}", MAPPING, throwable.getMessage());
        return SerializeUtils.write(Errorable.withError(throwable.getMessage()));
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
