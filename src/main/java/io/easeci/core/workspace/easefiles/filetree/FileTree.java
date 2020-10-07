package io.easeci.core.workspace.easefiles.filetree;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileTree implements NestedLocations {
    @JsonIgnore
    private ObjectMapper objectMapper;
    @Getter
    private Path entryPoint;
    @Getter
    private Node rootNode;

    FileTree(Path entryPoint) {
        this.objectMapper = new ObjectMapper();
        this.rootNode = new Node(Files.isDirectory(entryPoint) ? NodeType.DIRECTORY : NodeType.FILE, entryPoint);
        this.entryPoint = entryPoint;
    }

    private FileTree(Path entryPoint, boolean isDirExist) {
        if (!isDirExist) {
            this.objectMapper = new ObjectMapper();
            this.rootNode = null;
            this.entryPoint = entryPoint;
        } else {
            throw new IllegalArgumentException("Action is not available!");
        }
    }

    public String jsonify() {
        try {
            return this.objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
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
