package io.easeci.core.workspace.cluster;

import io.easeci.core.node.connect.NodeConnection;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;


/**
 * Main interface for manage clustering Easeci system.
 * Use it for deal with your workspace storage,
 * you can load or save (dump) current state of connection between master and worker nodes.
 */
public interface ClusterConnectionIO {

    /**
     * Use this method to initialize directory and file for cluster connection information storage.
     * */
    List<NodeConnection> initialize(Path path);

    /**
     * Use this method to read collection of node connections from storage.
     * @param path is a path where node connection configuration file
     *             is placed on workspace storage.
     * @return list of NodeConnection objects that holds all information about node connections created
     * before application startup etc.
     */
    List<NodeConnection> load(Path path);

    /**
     * Important! This method must be executed each time when state of connections in cluster is changed.
     * @param path            is a path where node connection configuration file
     *                        is placed on workspace storage.
     * @param nodeConnections is a POJO objects of node connections dedicated for save in workspace storage
     * @return List<NodeConnection> that holds all information about node connections created
     * before application startup etc.
     */
    List<NodeConnection> save(Path path, List<NodeConnection> nodeConnections) throws IOException;
}
