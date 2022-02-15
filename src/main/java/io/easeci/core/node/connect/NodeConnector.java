package io.easeci.core.node.connect;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NodeConnector {

    // metoda ma nam zwracać czy w ogóle zachodzi łączność core - worker
    // metoda może zmieniać stan z REQUESTED na ESTABLISHED
    public ClusterConnectionStateMonitor.ConnectionStateResponse initialCallback(ClusterConnectionStateMonitor.ConnectionStateRequest connectionStateRequest) {
        log.info("Checking connection from EaseCI Core node to nodeIp: {}", connectionStateRequest.getNodeIp());
        // todo strzał do workera
        return null;
    }

    // metoda ma zwracać po prostu status w jakim obecnie jest worker
    public void fetchNodeState() {

    }
}
