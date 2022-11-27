package io.easeci.core.node.connect;

import io.easeci.core.node.connect.dto.ConnectionStateRequest;
import io.easeci.server.TransferProtocol;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class NodeConnectorTest {

    static Stream<Arguments> arguments() {
        return Stream.of(
                Arguments.arguments("http://worker-01.easeci.io/api/v1/connection/state",
                        ConnectionStateRequest.of(
                                null,
                                null,
                                "worker-01.easeci.io",
                                "worker-01",
                                null
                        )),

                Arguments.arguments("http://worker-01.easeci.io:8443/api/v1/connection/state",
                        ConnectionStateRequest.of(
                                null,
                                "8443",
                                "worker-01.easeci.io",
                                "worker-01",
                                null
                        )),

                Arguments.arguments("https://worker-01.easeci.io:8443/api/v1/connection/state",
                        ConnectionStateRequest.of(
                                null,
                                "8443",
                                "worker-01.easeci.io",
                                "worker-01",
                                TransferProtocol.HTTPS
                        )),

                Arguments.arguments("https://worker-01.easeci.io/api/v1/connection/state",
                        ConnectionStateRequest.of(
                                null,
                                null,
                                "worker-01.easeci.io",
                                "worker-01",
                                TransferProtocol.HTTPS
                        )),

                Arguments.arguments("http://173.45.3.102/api/v1/connection/state",
                        ConnectionStateRequest.of(
                                "173.45.3.102",
                                null,
                                null,
                                "worker-01",
                                null
                        )),

                Arguments.arguments("http://173.45.3.102:8080/api/v1/connection/state",
                        ConnectionStateRequest.of(
                                "173.45.3.102",
                                "8080",
                                null,
                                "worker-01",
                                null
                        )),

                Arguments.arguments("https://173.45.3.102:8443/api/v1/connection/state",
                        ConnectionStateRequest.of(
                                "173.45.3.102",
                                "8443",
                                null,
                                "worker-01",
                                TransferProtocol.HTTPS
                        ))
        );
    }

    @ParameterizedTest
    @MethodSource("arguments")
    void shouldCorrectlyBuildUrl(String expectedUrl, ConnectionStateRequest request) throws UrlBuildException {
        ClusterInformation clusterInformation = Mockito.mock(ClusterInformation.class);
        Mockito.when(clusterInformation.apiVersionPrefix()).thenReturn("/api/v1");
        NodeConnector.NodeUrlBuilder nodeUrlBuilder = new NodeConnector.NodeUrlBuilder(clusterInformation);
        String URL = nodeUrlBuilder.buildUrl(request);
        assertEquals(expectedUrl, URL);
    }

}