package io.easeci.api.validation;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class ValidationErrorResponse {
    private List<ValidationError> errors;

    public static ValidationErrorResponse unrecognizedError() {
        ValidationErrorResponse response = new ValidationErrorResponse();
        response.setErrors(Collections.singletonList(ValidationError.builder()
                .errorMessage("Some unrecognized error occurred")
                .build()));
        return response;
    }

    public static ValidationErrorResponse jsonInvalidFieldError() {
        ValidationErrorResponse response = new ValidationErrorResponse();
        response.setErrors(Collections.singletonList(ValidationError.builder()
                .errorCode("JSON fields malformed")
                .errorMessage("JSON from your request body is not correct. Please check format of object again")
                .build()));
        return response;
    }

    public static ValidationErrorResponse jsonDeserializationError() {
        ValidationErrorResponse response = new ValidationErrorResponse();
        response.setErrors(Collections.singletonList(ValidationError.builder()
                .errorCode("JSON values malformed")
                .errorMessage("JSON from your request body is not correct. Field's values do not match.")
                .build()));
        return response;
    }
}
