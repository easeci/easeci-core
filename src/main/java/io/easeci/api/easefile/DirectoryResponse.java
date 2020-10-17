package io.easeci.api.easefile;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.easeci.api.Errorable;
import lombok.*;

import java.nio.file.Path;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(staticName = "of")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DirectoryResponse extends Errorable {
    private Path directoryPath;
    private Boolean isDirectoryCreated;
    private Boolean isDirectoryRemoved;
    private String errorMessage;

    private DirectoryResponse(boolean isDirectoryRemoved, String errorMessage) {
        this.isDirectoryRemoved = isDirectoryRemoved;
        this.errorMessage = errorMessage;
    }

    public static DirectoryResponse of(boolean isDirectoryRemoved, String errorMessage) {
        return new DirectoryResponse(isDirectoryRemoved, errorMessage);
    }

    public static DirectoryResponse withError(String errorMessage) {
        DirectoryResponse directoryResponse = new DirectoryResponse();
        directoryResponse.errorMessage = errorMessage;
        directoryResponse.isDirectoryCreated = false;
        return directoryResponse;
    }
}
