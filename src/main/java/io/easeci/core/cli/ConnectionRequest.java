package io.easeci.core.cli;

import io.easeci.api.validation.ValidationError;
import io.easeci.api.validation.Validator;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ConnectionRequest implements Validator {
    private String username;

    @Override
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>(0);
        if (username.length() < 5) {
            errors.add(ValidationError.builder()
                    .field("username")
                    .errorCode("invalid length")
                    .errorMessage("Username must be longer that 5")
                    .build());
        }
        if (username.length() > 15) {
            errors.add(ValidationError.builder()
                    .field("username")
                    .errorCode("invalid length")
                    .errorMessage("Username must be shorter that 15")
                    .build());
        }
        return errors;
    }
}
