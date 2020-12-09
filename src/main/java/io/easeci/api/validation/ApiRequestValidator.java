package io.easeci.api.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import ratpack.exec.Promise;
import ratpack.http.Request;

import java.util.ArrayList;
import java.util.List;

import static io.easeci.api.validation.ValidationErrorResponse.*;

@Slf4j
public class ApiRequestValidator {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> Promise<T> extractBody(Request request, Class<T> bodyType) {
        return request.getBody()
                .map(typedData -> objectMapper.readValue(typedData.getBytes(), bodyType))
                .next(ApiRequestValidator::validate);
    }

    @SneakyThrows
    public static Promise<byte[]> handleExceptionPromise(Throwable throwable) {
        return Promise.value(handleException(throwable));
    }

    @SneakyThrows
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
        return defaultError();
    }

    private static byte[] write(ValidationErrorResponse validationErrorResponse) throws JsonProcessingException {
        try {
            return objectMapper.writeValueAsBytes(validationErrorResponse);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return defaultError();
        }
    }

    private static byte[] defaultError() throws JsonProcessingException {
        return objectMapper.writeValueAsBytes(ValidationErrorResponse.unrecognizedError());
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
