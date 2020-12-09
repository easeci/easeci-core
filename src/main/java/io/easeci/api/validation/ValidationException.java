package io.easeci.api.validation;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {
    private ValidationErrorResponse validationErrorResponse;

    public ValidationException(ValidationErrorResponse validationErrorResponse) {
        this.validationErrorResponse = validationErrorResponse;
    }
}