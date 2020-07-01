package io.easeci.api.extension;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionResponse {
    private Boolean isSuccessfullyDone;
    private String message;
    private List<String> messages;

    public static ActionResponse of(Boolean isSuccessfullyDone, List<String> messages) {
        ActionResponse actionResponse = new ActionResponse();
        actionResponse.setIsSuccessfullyDone(isSuccessfullyDone);
        actionResponse.setMessages(messages);
        return actionResponse;
    }
}
