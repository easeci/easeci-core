package io.easeci.core.workspace.easefiles.filetree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class FileTreeWalker {
    private Path parentDirectory;
    private FileTree fileTree;

    public FileTreeWalker(Path parentDirectory) {
        this.parentDirectory = parentDirectory;
        this.fileTree = new FileTree(parentDirectory);
    }

    public FileTree dumpAll() throws IOException {
        return walk(this.parentDirectory, fileTree.getRootNode(), true);
    }

    public FileTree dumpOne() throws IOException {
        return walk(this.parentDirectory, fileTree.getRootNode(), false);
    }

    private FileTree walk(Path pathDirectory, Node node, boolean recursively) throws IOException {
        if (!Files.isDirectory(pathDirectory)) {
            return FileTree.empty(pathDirectory);
        }
        List<Path> paths = Files.list(pathDirectory).collect(Collectors.toList());
        for (Path path : paths) {
            boolean regularFile = Files.isRegularFile(path);
            if (regularFile) {
                Node nodeNew = new Node(NodeType.FILE, path);
                node.add(nodeNew);
                continue;
            }
            boolean directory = Files.isDirectory(path);
            if (directory) {
                Node nodeNew = new Node(NodeType.DIRECTORY, path);
                node.add(nodeNew);
                if (recursively) {
                    walk(path, nodeNew, true);
                }
            }
        }
        return fileTree;
    }
}
