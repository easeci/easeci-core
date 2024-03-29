package io.easeci.api.validation;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import io.easeci.commons.SerializeUtils;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import ratpack.exec.Promise;
import ratpack.handling.Context;
import ratpack.http.Request;

import java.util.ArrayList;
import java.util.List;

import static io.easeci.api.validation.ValidationErrorResponse.*;

@Slf4j
public class ApiRequestValidator {

    public static <T> Promise<T> extractBody(Request request, Class<T> bodyType) {
        return request.getBody()
                .map(typedData -> SerializeUtils.read(typedData.getBytes(), bodyType)
                        .orElseThrow(() -> new IllegalStateException("Cannot read byte[] to class: " + bodyType.getName())))
                .next(ApiRequestValidator::validate);
    }

    public static <T> Promise<T> extractBody(Context ctx, Class<T> bodyType) {
        return ctx.getRequest().getBody()
                .map(typedData -> SerializeUtils.read(typedData.getBytes(), bodyType)
                        .orElseThrow(() -> new IllegalStateException("Cannot read byte[] to class: " + bodyType.getName())))
                .next(ApiRequestValidator::validate)
                .mapError(throwable -> {
                    byte[] bytes = ApiRequestValidator.handleException(throwable);
                    throw new ValidationErrorSignal("Request ends with validation error, validation errors returned to client", bytes);
                });
    }

    @Getter
    @AllArgsConstructor
    public static class ValidationErrorSignal extends RuntimeException {
        private String message;
        private byte[] response;
    }

    public static Promise<byte[]> handleExceptionPromise(Throwable throwable) {
        return Promise.value(handleException(throwable));
    }

    public static byte[] handleException(Throwable throwable) {
        if (throwable instanceof ValidationException) {
            ValidationErrorResponse validationErrorResponse = ((ValidationException) throwable).getValidationErrorResponse();
            return write(validationErrorResponse);
        }
        if (throwable instanceof UnrecognizedPropertyException) {
            ValidationErrorResponse validationErrorResponse = jsonInvalidFieldError();
            return write(validationErrorResponse);
        }
        if (throwable instanceof InvalidFormatException) {
            ValidationErrorResponse validationErrorResponse = jsonDeserializationError();
            return write(validationErrorResponse);
        }
        if (throwable instanceof MismatchedInputException) {
            ValidationErrorResponse validationErrorResponse = jsonNoContentError();
            return write(validationErrorResponse);
        }
        if (throwable instanceof InvalidDefinitionException || throwable instanceof JsonParseException) {
            ValidationErrorResponse validationErrorResponse = jsonDefinitionError();
            return write(validationErrorResponse);
        }
        else {
            throwable.printStackTrace();
            return defaultError();
        }
    }

    private static byte[] write(ValidationErrorResponse validationErrorResponse) {
        return SerializeUtils.write(validationErrorResponse);
    }

    private static byte[] defaultError() {
        return SerializeUtils.write(ValidationErrorResponse.unrecognizedError());
    }

    private static <T> void validate(T requestBody) {
        Validator validator;
        try {
            validator = (Validator) requestBody;
        } catch (ClassCastException exception) {
            List<ValidationError> errorList = new ArrayList<>(1);
            errorList.add(ValidationError.builder()
                                         .errorMessage(
                                                 "Validator implementation is invalid, cannot make validation process finished. "
                                                 + "Validation not implemented for class: " + requestBody.getClass().getName()
                                         )
                                         .build());
            ValidationErrorResponse response = new ValidationErrorResponse();
            response.setErrors(errorList);
            throw new ValidationException(response);
        }

        List<ValidationError> errorList = validator.validate();
        if (errorList == null || errorList.isEmpty()) {
            return;
        }
        ValidationErrorResponse response = new ValidationErrorResponse();
        response.setErrors(errorList);
        throw new ValidationException(response);
    }
}
