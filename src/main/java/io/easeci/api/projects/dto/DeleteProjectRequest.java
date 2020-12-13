package io.easeci.api.projects.dto;

import io.easeci.api.validation.ValidationError;
import io.easeci.api.validation.Validator;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

import static io.easeci.api.validation.CommonValidatorSet.combine;
import static io.easeci.api.validation.CommonValidatorSet.nullCheck;

@Data
public class DeleteProjectRequest implements Validator {
    private Long projectGroupId;
    private Long projectId;
    private Boolean isHardRemoval;

    @Override
    public List<ValidationError> validate() {
        return combine(
                Arrays.asList(
                        nullCheck(this.projectGroupId, "projectGroupId"),
                        nullCheck(this.projectId, "projectId")
                )
        );
    }
}
