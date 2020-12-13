package io.easeci.api.projects.dto;

import io.easeci.api.validation.ValidationError;
import io.easeci.api.validation.Validator;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

import static io.easeci.api.validation.CommonValidatorSet.combine;
import static io.easeci.api.validation.CommonValidatorSet.nullCheck;

@Data
public class DeletePipelinePointerRequest implements Validator {
    private Long projectId;
    private Long pipelinePointerId;

    @Override
    public List<ValidationError> validate() {
        return combine(
                Arrays.asList(
                     nullCheck(this.projectId, "projectId"),
                     nullCheck(this.pipelinePointerId, "pipelinePointerId")
                )
        );
    }
}
