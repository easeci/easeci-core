package io.easeci.api.projects.dto;

import io.easeci.api.validation.ValidationError;
import io.easeci.api.validation.Validator;
import lombok.Data;

import java.util.List;

import static io.easeci.api.validation.CommonValidatorSet.nullCheck;

@Data
public class DeleteProjectGroupRequest implements Validator {
    private Long projectGroupId;
    private Boolean isHardRemoval;

    @Override
    public List<ValidationError> validate() {
        return nullCheck(this.projectGroupId, "projectGroupId");
    }
}
