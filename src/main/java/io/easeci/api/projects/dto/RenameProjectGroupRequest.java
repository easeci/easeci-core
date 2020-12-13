package io.easeci.api.projects.dto;

import io.easeci.api.validation.ValidationError;
import io.easeci.api.validation.Validator;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

import static io.easeci.api.validation.CommonValidatorSet.*;
import static io.easeci.api.validation.CommonValidatorSet.validateStringLength;

@Data
public class RenameProjectGroupRequest implements Validator {
    private Long projectGroupId;
    private String name;

    @Override
    public List<ValidationError> validate() {
        return combine(
                Arrays.asList(
                        nullCheck(projectGroupId, "projectGroupId"),
                        validateStringLength(this.name, 3, 30, "name", false)
                ));
    }
}
