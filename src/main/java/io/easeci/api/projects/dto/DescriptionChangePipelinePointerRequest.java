package io.easeci.api.projects.dto;

import io.easeci.api.validation.ValidationError;
import io.easeci.api.validation.Validator;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

import static io.easeci.api.validation.CommonValidatorSet.*;

@Data
public class DescriptionChangePipelinePointerRequest implements Validator {
    private Long projectId;
    private Long pipelinePointerId;
    private String description;

    @Override
    public List<ValidationError> validate() {
        return combine(
                Arrays.asList(
                        nullCheck(this.projectId, "projectId"),
                        nullCheck(this.pipelinePointerId, "pipelinePointerId"),
                        validateStringLength(this.description, 0, 500, "description", false)
                )
        );
    }
}
