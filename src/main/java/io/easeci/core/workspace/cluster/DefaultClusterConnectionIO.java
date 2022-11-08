package io.easeci.core.workspace.cluster;

import com.fasterxml.jackson.core.type.TypeReference;
import io.easeci.core.node.connect.NodeConnection;
import io.easeci.commons.SerializeUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DefaultClusterConnectionIO implements ClusterConnectionIO {

    @Override
    public List<NodeConnection> initialize(Path path) {
        List<NodeConnection> nodeConnections = new ArrayList<>();
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            log.error("Error occurred while initializing cluster settings here: {}", path, e);
        }
        try {
            Files.createDirectories(path.getParent());
            Path clusterSettingsPath = Files.createFile(path);
            log.info("Correctly initialized file for store cluster settings here: {}", clusterSettingsPath);
        } catch (IOException e) {
            log.error("Error occurred while creating cluster settings here: {}", path, e);
        }
        return nodeConnections;
    }

    @Override
    public List<NodeConnection> load(Path path) {
        try {
            byte[] bytes = Files.readAllBytes(path);
            if (bytes.length != 0) {
                List<NodeConnection> nodeConnections = SerializeUtils.read(bytes, new TypeReference<List<NodeConnection>>() {})
                        .orElseThrow(() -> new IllegalStateException("Content file of: " + path + " is malformed, cannot read data!"));
                log.info("Correctly read data from cluster settings file");
                return nodeConnections;
            } else {
                log.info("Cluster settings file is empty, initialized new node connection list");
            }
        } catch (IOException e) {
            log.error("Could not correctly read data from cluster settings file", e);
        }
        return new ArrayList<>();
    }

    @Override
    public List<NodeConnection> save(Path path, List<NodeConnection> nodeConnections) throws IOException {
        byte[] bytes = SerializeUtils.write(nodeConnections);
        Files.write(path, bytes);
        return nodeConnections;
    }
}
