package io.easeci.api.parsing;

import io.easeci.api.validation.ValidationError;
import io.easeci.api.validation.Validator;
import io.easeci.core.engine.easefile.loader.Source;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

import static io.easeci.api.validation.CommonValidatorSet.*;

@Data
public class RunParseProcess implements Validator {
    private Source source;
    private String localStoragePath;
    private String gitRepositoryUrl;
    private String encodedEasefileContent;


    @Override
    public List<ValidationError> validate() {
        return combine(Arrays.asList(
                nullCheck(this.source, "source"),
                atLeastOne(Arrays.asList(this.localStoragePath,
                                         this.gitRepositoryUrl,
                                         this.encodedEasefileContent),
                           Arrays.asList("localStoragePath",
                                         "gitRepositoryUrl",
                                         "encodedEasefileContent")
                )
        ));
    }
}
