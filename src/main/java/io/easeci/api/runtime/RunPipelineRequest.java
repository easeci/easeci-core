package io.easeci.api.runtime;

import io.easeci.api.validation.ValidationError;
import io.easeci.api.validation.Validator;
import lombok.Data;

import java.util.List;
import java.util.UUID;

import static io.easeci.api.validation.CommonValidatorSet.nullCheck;

@Data
public class RunPipelineRequest implements Validator {
    private UUID pipelineId;

    @Override
    public List<ValidationError> validate() {
        return nullCheck(this.pipelineId, "pipelineId");
    }
}
