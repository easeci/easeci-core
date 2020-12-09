package io.easeci.api.validation;

import java.util.List;

public interface Validator {

    List<ValidationError> validate();
}
