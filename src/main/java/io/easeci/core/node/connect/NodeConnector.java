package io.easeci.core.node.connect;

import io.easeci.core.engine.scheduler.ScheduleRequest;
import io.easeci.core.engine.scheduler.ScheduleResponse;
import io.easeci.core.node.connect.dto.ConnectionStateRequest;
import io.easeci.core.node.connect.dto.ConnectionStateResponse;
import io.easeci.commons.SerializeUtils;
import io.easeci.server.TransferProtocol;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.*;
import com.google.common.net.InetAddresses;

import java.util.concurrent.ExecutionException;

import static io.easeci.core.engine.scheduler.ScheduleErrorCode.*;
import static io.easeci.core.node.connect.NodeConnectionState.CONNECTION_ERROR;
import static io.easeci.core.node.connect.NodeProcessingState.UNKNOWN;
import static java.util.Objects.nonNull;

@Slf4j
public class NodeConnector {

    private AsyncHttpClient asyncHttpClientNoSsl;
    private ClusterInformation clusterInformation;
    private NodeUrlBuilder nodeUrlBuilder;

    public NodeConnector() {
        this.asyncHttpClientNoSsl = buildDefaultHttpClient();
        this.clusterInformation = new ClusterInformationDefault();
        this.nodeUrlBuilder = new NodeUrlBuilder(clusterInformation);
    }

    public ConnectionStateResponse checkWorkerState(ConnectionStateRequest connectionStateRequest) throws UrlBuildException {
        final String URL = nodeUrlBuilder.buildUrl(connectionStateRequest);
        log.info("Checking connection from EaseCI Core node to: {}", URL);
        final byte[] payload = SerializeUtils.write(connectionStateRequest);
        final AsyncHttpClient asyncHttpClient = chooseClient(connectionStateRequest.getTransferProtocol());
        try {
            Response response = asyncHttpClient.executeRequest(new RequestBuilder()
                                                                   .setMethod("POST")
                                                                   .setUrl(URL)
                                                                   .setBody(payload)
                                                                   .setHeader("Content-Type", "application/json")
                                                                   .build())
                                               .get();
            if (response.getStatusCode() == 200) {
                return SerializeUtils.read(response.getResponseBodyAsBytes(), ConnectionStateResponse.class)
                                     .orElseGet(() -> {
                                         log.error("Could not serialize response from worker node, we need to mark connection as {}", CONNECTION_ERROR);
                                         return ClusterConnectionStateMonitor.createResponseFailure(CONNECTION_ERROR, connectionStateRequest);
                                     });
            } else {
                log.info("Connection error occurred, worker node returned http code: {}, and responses: {}", response.getStatusText(),
                         new String(response.getResponseBodyAsBytes()));
            }
        } catch (InterruptedException e) {
            log.error("InterruptedException was thrown while sending request to worker node: " + URL, e);
        } catch (ExecutionException e) {
            log.error("ExecutionException was thrown while sending request to worker node: " + URL, e);
        } catch (Exception e) {
            log.error("Exception was thrown: ", e);
        }
        return ClusterConnectionStateMonitor.createResponseFailure(CONNECTION_ERROR, connectionStateRequest);
    }

    public ScheduleResponse sendPipeline(NodeConnection nodeConnectionChosen, ScheduleRequest scheduleRequest) throws UrlBuildException {
        log.info("Sending pipeline job request to worker node: {}", nodeConnectionChosen);
        final String URL = nodeUrlBuilder.buildUrl(nodeConnectionChosen);
        final byte[] payload = SerializeUtils.write(scheduleRequest);
        final AsyncHttpClient asyncHttpClient = chooseClient(nodeConnectionChosen.getTransferProtocol());
        try {
            Response response = asyncHttpClient.executeRequest(new RequestBuilder()
                            .setMethod("POST")
                            .setUrl(URL)
                            .setBody(payload)
                            .setHeader("Content-Type", "application/json")
                            .build())
                    .get();
            if (response.getStatusCode() == 200) {
                return SerializeUtils.read(response.getResponseBodyAsBytes(), ScheduleResponse.class)
                        .orElseGet(() -> {
                            log.error("Could not serialize response from worker node, we need to mark connection as {}", CONNECTION_ERROR);
                            return ScheduleResponse.createResponseFailure(CONNECTION_ERROR, UNKNOWN, WORKER_NODE_RESPONSE_NOT_SERIALIZABLE);
                        });
            } else {
                log.info("Connection error occurred, worker node returned http code: {}, and responses: {}", response.getStatusText(),
                        new String(response.getResponseBodyAsBytes()));
                return ScheduleResponse.createResponseFailure(CONNECTION_ERROR, UNKNOWN, WORKER_NODE_HTTP_ERROR_RESPONSE);
            }
        } catch (InterruptedException e) {
            log.error("InterruptedException was thrown while sending request to worker node: " + URL, e);
        } catch (ExecutionException e) {
            log.error("ExecutionException was thrown while sending request to worker node: " + URL, e);
        } catch (Exception e) {
            log.error("Unexpected error occurred: ", e);
        }
        return ScheduleResponse.createResponseFailure(CONNECTION_ERROR, UNKNOWN, WORKER_NODE_REQUEST_TERMINATED_WITH_ERROR);
    }

    private AsyncHttpClient chooseClient(TransferProtocol transferProtocol) {
        if (TransferProtocol.HTTP.equals(transferProtocol)) {
            return this.asyncHttpClientNoSsl;
        }
        if (TransferProtocol.HTTPS.equals(transferProtocol)) {
            throw new UnsupportedOperationException("SSL transfer between Core and Worker not implemented yet");
        }
        throw new UnsupportedOperationException("Cannot detect transfer protocol from " + transferProtocol.name());
    }

    private AsyncHttpClient buildDefaultHttpClient() {
        DefaultAsyncHttpClientConfig.Builder clientBuilder = Dsl.config()
                                                                .setHandshakeTimeout(1000)
                                                                .setConnectTimeout(1000);
        return Dsl.asyncHttpClient(clientBuilder);
    }

    public static class NodeUrlBuilder {
        private ClusterInformation clusterInformation;

        public NodeUrlBuilder(ClusterInformation clusterInformation) {
            this.clusterInformation = clusterInformation;
        }

        public String buildUrl(NodeConnection nodeConnection) throws UrlBuildException {
            return getHostAddress(ConnectionStateRequest.of(nodeConnection.getNodeIp(),
                    nodeConnection.getNodePort(),
                    nodeConnection.getDomainName(),
                    nodeConnection.getNodeName(),
                    nodeConnection.getTransferProtocol()))
                    .concat(clusterInformation.apiVersionPrefix().concat("/pipeline/receive"));
        }

        public String buildUrl(ConnectionStateRequest connectionStateRequest) throws UrlBuildException {
            return getHostAddress(connectionStateRequest)
                    .concat(clusterInformation.apiVersionPrefix().concat("/connection/state"));
        }

        private String getHostAddress(ConnectionStateRequest connectionStateRequest) throws UrlBuildException {
            String host = "";
            if (nonNull(connectionStateRequest.getNodeIp()) && !connectionStateRequest.getNodeIp()
                                                                                      .isEmpty()) {
                boolean isInetAddress = InetAddresses.isInetAddress(connectionStateRequest.getNodeIp());
                if (isInetAddress) {
                    host = connectionStateRequest.getNodeIp();
                }
            }
            else if (nonNull(connectionStateRequest.getDomainName()) && !connectionStateRequest.getDomainName()
                                                                                               .isEmpty()) {
                host = connectionStateRequest.getDomainName();
            }
            else {
                throw new UrlBuildException("Cannot detect host address from object: " + connectionStateRequest);
            }

            host = joinPort(connectionStateRequest, host);
            host = joinProtocol(connectionStateRequest, host);
            return host;
        }

        private String joinProtocol(ConnectionStateRequest connectionStateRequest, String host) {
            if (nonNull(connectionStateRequest.getTransferProtocol())) {
                return connectionStateRequest.getTransferProtocol()
                                             .prefix()
                                             .concat(host);
            }
            else {
                return TransferProtocol.HTTP.prefix()
                                            .concat(host);
            }
        }

        private String joinPort(ConnectionStateRequest connectionStateRequest, String host) {
            if (nonNull(connectionStateRequest.getNodePort()) && !connectionStateRequest.getNodePort()
                                                                                        .isEmpty()) {
                return host.concat(":")
                           .concat(connectionStateRequest.getNodePort());
            }
            return host;
        }
    }
}
