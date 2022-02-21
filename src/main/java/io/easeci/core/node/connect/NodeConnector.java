package io.easeci.core.node.connect;

import io.easeci.core.workspace.SerializeUtils;
import io.easeci.server.TransferProtocol;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.RequestBuilder;
import com.google.common.net.InetAddresses;

import static io.easeci.core.node.NodeUtils.apiVersionPrefix;
import static java.util.Objects.nonNull;

@Slf4j
public class NodeConnector {

    private AsyncHttpClient asyncHttpClientNoSsl;

    public NodeConnector() {
        this.asyncHttpClientNoSsl = buildDefaultHttpClient();
    }

    // metoda ma nam zwracać czy w ogóle zachodzi łączność core - worker
    // metoda może zmieniać stan z REQUESTED na ESTABLISHED
    public ClusterConnectionStateMonitor.ConnectionStateResponse initialCallback(ClusterConnectionStateMonitor.ConnectionStateRequest connectionStateRequest) {
        log.info("Checking connection from EaseCI Core node to nodeIp: {}", connectionStateRequest.getNodeIp());
        final String URL = NodeUrlBuilder.buildUrl(connectionStateRequest);
        final byte[] payload = SerializeUtils.write(connectionStateRequest);
        log.info("URL address of Worker Node prepared: {}", URL);
        final AsyncHttpClient asyncHttpClient = chooseClient(connectionStateRequest.getTransferProtocol());
        asyncHttpClient.executeRequest(new RequestBuilder()
                        .setMethod("POST")
                        .setUrl(URL)
                        .setBody(payload)
                .build());

        return null;
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

        public static String buildUrl(ClusterConnectionStateMonitor.ConnectionStateRequest connectionStateRequest) {
            return getHostAddress(connectionStateRequest)
                    .concat(apiVersionPrefix().concat("/connection/state"));
        }

        private static String getHostAddress(ClusterConnectionStateMonitor.ConnectionStateRequest connectionStateRequest) {
            String host = "";
            if (nonNull(connectionStateRequest.getNodeIp()) && !connectionStateRequest.getNodeIp().isEmpty()) {
                boolean isInetAddress = InetAddresses.isInetAddress(connectionStateRequest.getNodeIp());
                if (isInetAddress) {
                    host = connectionStateRequest.getNodeIp();
                }
            } else if (nonNull(connectionStateRequest.getDomainName()) && !connectionStateRequest.getDomainName().isEmpty()) {
                host = connectionStateRequest.getDomainName();
            } else {
                throw new IllegalArgumentException("Cannot detect host address from object: " + connectionStateRequest);
            }

            host = joinPort(connectionStateRequest, host);
            host = joinProtocol(connectionStateRequest, host);
            return host;
        }

        private static String joinProtocol(ClusterConnectionStateMonitor.ConnectionStateRequest connectionStateRequest, String host) {
            if (nonNull(connectionStateRequest.getTransferProtocol())) {
                return connectionStateRequest.getTransferProtocol().prefix().concat(host);
            } else return TransferProtocol.HTTP.prefix().concat(host);
        }

        private static String joinPort(ClusterConnectionStateMonitor.ConnectionStateRequest connectionStateRequest, String host) {
            if (nonNull(connectionStateRequest.getNodePort()) && !connectionStateRequest.getNodePort().isEmpty()) {
                return host.concat(":")
                        .concat(connectionStateRequest.getNodePort());
            }
            return host;
        }
    }
}
