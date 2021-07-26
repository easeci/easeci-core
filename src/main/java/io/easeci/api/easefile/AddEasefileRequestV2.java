package io.easeci.api.easefile;

import io.easeci.api.validation.ValidationError;
import io.easeci.api.validation.Validator;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

import static io.easeci.api.validation.CommonValidatorSet.combine;
import static io.easeci.api.validation.CommonValidatorSet.nullCheck;

@Data
public class AddEasefileRequestV2  implements Validator {
    private String filename;
    private String encodedEasefileContent;

    @Override
    public List<ValidationError> validate() {
        return combine(
                Arrays.asList(
                        nullCheck(this.filename, "filename"),
                        nullCheck(this.filename, "encodedEasefileContent")
                ));
    }
}
