package io.easeci.core.node.connect;

public class NodeConnector {

    // metoda ma nam zwracać czy w ogóle zachodzi łączność core - worker
    // metoda może zmieniać stan z REQUESTED na ESTABLISHED
    public void initialCallback(ClusterConnectionStateMonitor.ConnectionStateRequest connectionStateRequest) {

    }

    // metoda ma zwracać po prostu status w jakim obecnie jest worker
    public void fetchNodeState() {

    }
}
