package io.easeci.api.validation;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "errorMessage")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationError {
    private String field;
    private String errorCode;
    private String errorMessage;
}
