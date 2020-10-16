package io.easeci.api.easefile;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.easeci.core.workspace.easefiles.filetree.FileTree;
import lombok.Data;

import java.nio.file.Path;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EasefileWorkspaceResponse {
    private String errorMessage;
    private Path rootPath;
    private FileTree fileTree;

    private EasefileWorkspaceResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public EasefileWorkspaceResponse(Path rootPath) {
        this.rootPath = rootPath;
    }

    public EasefileWorkspaceResponse(FileTree fileTree) {
        this.fileTree = fileTree;
    }

    public static EasefileWorkspaceResponse withError(String errorMessage) {
        return new EasefileWorkspaceResponse(errorMessage);
    }
}
