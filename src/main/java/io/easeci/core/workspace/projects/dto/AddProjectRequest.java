package io.easeci.core.workspace.projects.dto;

import io.easeci.api.validation.ValidationError;
import io.easeci.api.validation.Validator;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

import static io.easeci.api.validation.CommonValidatorSet.*;

@Data
public class AddProjectRequest implements Validator {
    private Long projectGroupId;
    private String name;
    private String tag;
    private String description;

    @Override
    public List<ValidationError> validate() {
        return combine(
                Arrays.asList(
                        nullCheck(this.projectGroupId, "projectGroupId"),
                        validateStringLength(this.name, 3, 30, "name", false),
                        validateStringLength(this.tag, 3, 15, "tag", true),
                        validateStringLength(this.description, 0, 500, "description", true)
                ));
    }
}
