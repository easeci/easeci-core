package io.easeci.core.workspace.easefiles.filetree;

import io.easeci.commons.SerializeUtils;
import lombok.Getter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileTree implements NestedLocations {

    @Getter
    private Path entryPoint;
    @Getter
    private Node rootNode;

    FileTree(Path entryPoint) {
        this.rootNode = new Node(Files.isDirectory(entryPoint) ? NodeType.DIRECTORY : NodeType.FILE, entryPoint);
        this.entryPoint = entryPoint;
    }

    private FileTree(Path entryPoint, boolean isDirExist) {
        if (!isDirExist) {
            this.rootNode = null;
            this.entryPoint = entryPoint;
        } else {
            throw new IllegalArgumentException("Action is not available!");
        }
    }

    public String jsonify() {
        return new String(SerializeUtils.write(this));
    }

    public static FileTree empty(Path entryPoint) {
        return new FileTree(entryPoint);
    }

    public static FileTree notExisting(Path entryPoint) {
        return new FileTree(entryPoint, false);
    }

    @Override
    public List<Path> nextLocations() {
        return rootNode.nextLocations();
    }
}
