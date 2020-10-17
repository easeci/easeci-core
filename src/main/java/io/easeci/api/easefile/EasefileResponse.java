package io.easeci.api.easefile;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.easeci.core.workspace.easefiles.EasefileStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(staticName = "of")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EasefileResponse {
    private EasefileStatus easefileStatus;
    private String errorMessage;
    private String encodedEasefileContent;

    public static EasefileResponse of(String fileContentEncoded, EasefileStatus status) {
        EasefileResponse easefileResponse = new EasefileResponse();
        easefileResponse.setEncodedEasefileContent(fileContentEncoded);
        easefileResponse.setEasefileStatus(status);
        return easefileResponse;
    }

    public static EasefileResponse withError(String errorMessage, EasefileStatus status) {
        EasefileResponse easefileResponse = new EasefileResponse();
        easefileResponse.setErrorMessage(errorMessage);
        easefileResponse.setEasefileStatus(status);
        return easefileResponse;
    }
}
