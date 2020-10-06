package io.easeci.core.workspace.easefiles.filetree;

import lombok.Getter;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

@Getter
class Node {
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
        }
        this.hasNext = hasNext();
    }

    public void add(Node node) {
        this.childNodes.add(node);
    }

    public boolean hasNext() {
        return childNodes != null;
    }
}
