package io.easeci.core.node.connect;

import io.easeci.core.node.connect.dto.ConnectionStateRequest;
import io.easeci.core.node.connect.dto.ConnectionStateResponse;
import io.easeci.core.workspace.SerializeUtils;
import io.easeci.server.TransferProtocol;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.*;
import com.google.common.net.InetAddresses;

import java.util.concurrent.ExecutionException;

import static io.easeci.core.node.NodeUtils.apiVersionPrefix;
import static io.easeci.core.node.connect.NodeConnectionState.CONNECTION_ERROR;
import static java.util.Objects.nonNull;

@Slf4j
public class NodeConnector {

    private AsyncHttpClient asyncHttpClientNoSsl;

    public NodeConnector() {
        this.asyncHttpClientNoSsl = buildDefaultHttpClient();
    }

    public ConnectionStateResponse initialCallback(ConnectionStateRequest connectionStateRequest) {
        final String URL = NodeUrlBuilder.buildUrl(connectionStateRequest);
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
            log.info("InterruptedException was thrown while sending request to worker node: {}", URL);
            e.printStackTrace();
        } catch (ExecutionException e) {
            log.info("ExecutionException was thrown while sending request to worker node: {}", URL);
            e.printStackTrace();
        }
        return ClusterConnectionStateMonitor.createResponseFailure(CONNECTION_ERROR, connectionStateRequest);
    }

    // metoda ma zwracać po prostu status w jakim obecnie jest worker
    public void fetchNodeState() {

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

        public static String buildUrl(ConnectionStateRequest connectionStateRequest) {
            return getHostAddress(connectionStateRequest)
                    .concat(apiVersionPrefix().concat("/connection/state"));
        }

        private static String getHostAddress(ConnectionStateRequest connectionStateRequest) {
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
                throw new IllegalArgumentException("Cannot detect host address from object: " + connectionStateRequest);
            }

            host = joinPort(connectionStateRequest, host);
            host = joinProtocol(connectionStateRequest, host);
            return host;
        }

        private static String joinProtocol(ConnectionStateRequest connectionStateRequest, String host) {
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

        private static String joinPort(ConnectionStateRequest connectionStateRequest, String host) {
            if (nonNull(connectionStateRequest.getNodePort()) && !connectionStateRequest.getNodePort()
                                                                                        .isEmpty()) {
                return host.concat(":")
                           .concat(connectionStateRequest.getNodePort());
            }
            return host;
        }
    }
}
