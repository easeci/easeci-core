package io.easeci.core.cli;

import io.easeci.api.validation.ValidationError;
import io.easeci.api.validation.Validator;
import lombok.Data;

import java.util.List;
import java.util.UUID;

import static io.easeci.api.validation.CommonValidatorSet.validateUuid;

@Data
public class ConnectionCloseRequest implements Validator {
    private UUID connectionUuid;

    @Override
    public List<ValidationError> validate() {
        return validateUuid(this.connectionUuid, "connectionUuid", false);
    }
}
