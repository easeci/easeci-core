package io.easeci.api.easefile;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.easeci.core.workspace.easefiles.EasefileStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AddEasefileResponse {
    private String easefileStatus;
    private String errorMessage;
    private Path path;

    public static AddEasefileResponse withError(String errorMessage, EasefileStatus easefileStatus) {
        AddEasefileResponse addEasefileResponse = new AddEasefileResponse();
        addEasefileResponse.easefileStatus = easefileStatus.name();
        addEasefileResponse.errorMessage = errorMessage;
        return addEasefileResponse;
    }

    public static AddEasefileResponse of(Path path, EasefileStatus easefileStatus) {
        AddEasefileResponse addEasefileResponse = new AddEasefileResponse();
        addEasefileResponse.easefileStatus = easefileStatus.name();
        addEasefileResponse.path = path;
        return addEasefileResponse;
    }

    public static AddEasefileResponse of(EasefileStatus easefileStatus) {
        AddEasefileResponse addEasefileResponse = new AddEasefileResponse();
        addEasefileResponse.easefileStatus = easefileStatus.name();
        return addEasefileResponse;
    }
}
