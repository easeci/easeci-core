package io.easeci.core.workspace.easefiles.filetree;

import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
class Node implements NestedLocations {
    private NodeType nodeType;
    private Path nodePath;
    private List<Node> childNodes;
    private boolean hasNext;

    Node(NodeType nodeType, Path nodePath) {
        this.nodeType = nodeType;
        this.nodePath = nodePath;
        if (nodeType.equals(NodeType.FILE)) {
            this.childNodes = null;
        } else {
            this.childNodes = new LinkedList<>();
            this.hasNext = hasNext();
        }
    }

    public void add(Node node) {
        this.childNodes.add(node);
    }

    public boolean hasNext() {
        try {
            return childNodes != null && Files.list(nodePath).count() > 0;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Path> nextLocations() {
        if (!hasNext()) {
            return Collections.emptyList();
        }
        return childNodes.stream()
                .map(Node::getNodePath)
                .sorted()
                .collect(Collectors.toList());
    }
}
