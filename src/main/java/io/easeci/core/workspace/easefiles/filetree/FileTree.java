package io.easeci.core.workspace.easefiles.filetree;

import lombok.Getter;

import java.nio.file.Files;
import java.nio.file.Path;

@Getter
public class FileTree {
    private Path entryPoint;
    private Node rootNode;

    FileTree(Path entryPoint) {
        this.rootNode = new Node(Files.isDirectory(entryPoint) ? NodeType.DIRECTORY : NodeType.FILE, entryPoint);
        this.entryPoint = entryPoint;
    }

    public static FileTree empty(Path entryPoint) {
        return new FileTree(entryPoint);
    }
}
