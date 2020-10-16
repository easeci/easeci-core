package io.easeci.api.easefile;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.easeci.core.workspace.easefiles.EasefileStatus;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EasefileResponse {
    private EasefileStatus easefileStatus;
    private String errorMessage;
    private byte[] encodedEasefileContent;
}
