package io.easeci.api.projects.dto;

import io.easeci.api.validation.ValidationError;
import io.easeci.api.validation.Validator;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

import static io.easeci.api.validation.CommonValidatorSet.*;

@Data
public class DescriptionChangeProjectGroupRequest implements Validator {
    private Long projectGroupId;
    private String description;

    @Override
    public List<ValidationError> validate() {
        return combine(
                Arrays.asList(
                        nullCheck(projectGroupId, "projectGroupId"),
                        validateStringLength(this.description, 0, 500, "description", false)
                ));
    }
}
