package io.easeci.api.easefile;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.easeci.api.Errorable;
import io.easeci.core.workspace.easefiles.filetree.FileTree;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.nio.file.Path;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EasefileWorkspaceResponse extends Errorable {
    private Path rootPath;
    private FileTree fileTree;

    private EasefileWorkspaceResponse(String errorMessage) {
        super.setErrorMessage(errorMessage);
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
