package io.easeci.api.validation;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

public class CommonValidatorSet {

    public static List<ValidationError> validateUuid(UUID value, String fieldName, boolean optional) {
        List<ValidationError> errors = new ArrayList<>(0);
        if (!optional) {
            List<ValidationError> nullErrors = nullCheck(value, fieldName);
            if (!nullErrors.isEmpty()) {
                return nullErrors;
            }
        }
        else {
            if (value == null) {
                return errors;
            }
        }
        if (value.toString().length() != 36) {
            errors.add(ValidationError.builder()
                    .field(fieldName)
                    .errorCode("value length invalid")
                    .errorMessage("UUID value must consists of 36 characters")
                    .build());
        }
        return errors;
    }

    public static List<ValidationError> validateStringLength(String value, int minSize, int maxSize, String fieldName, boolean optional) {
        List<ValidationError> errors = new ArrayList<>(0);
        if (!optional) {
            List<ValidationError> nullErrors = nullCheck(value, fieldName);
            if (!nullErrors.isEmpty()) {
                return nullErrors;
            }
        } else {
            if (value == null) {
                return errors;
            }
        }
        if (value.length() < minSize) {
            errors.add(ValidationError.builder()
                    .field(fieldName)
                    .errorCode("String length too short")
                    .errorMessage(fieldName + " must consists of characters between " + minSize + " and " + maxSize)
                    .build());
        }
        if (value.length() > maxSize) {
            errors.add(ValidationError.builder()
                    .field(fieldName)
                    .errorCode("String length too long")
                    .errorMessage(fieldName + " must consists of characters between " + minSize + " and " + maxSize)
                    .build());
        }
        return errors;
    }

    public static List<ValidationError> validateStringPattern(String value, String regexp, String fieldName, boolean optional) {
        List<ValidationError> errors = new ArrayList<>(0);
        if (!optional) {
            List<ValidationError> nullErrors = nullCheck(value, fieldName);
            if (!nullErrors.isEmpty()) {
                return nullErrors;
            }
        } else {
            if (value == null) {
                return errors;
            }
        }
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(value);
        if (!matcher.matches()) {
            errors.add(ValidationError.builder()
                    .field(fieldName)
                    .errorCode("value not matches regexp")
                    .errorMessage("Value must match: " + regexp)
                    .build());
        }
        return errors;
    }

    public static List<ValidationError> nullCheck(Object value, String fieldName) {
        if (value == null) {
            return Collections.singletonList(ValidationError.builder()
                    .field(fieldName)
                    .errorCode("value is null")
                    .errorMessage("Field cannot be null")
                    .build());
        }
        return Collections.emptyList();
    }

    // At least one value must not be null
    public static List<ValidationError> atLeastOne(List<Object> objects, List<String> fieldNames) {
        boolean isAnyNonNull = objects.stream().anyMatch(Objects::nonNull);
        if (isAnyNonNull) {
            return Collections.emptyList();
        }
        return Collections.singletonList(ValidationError.builder()
                .errorCode("all required values are null")
                .errorMessage("At least one of values must be not null, fields: " + fieldNames)
                .build());
    }

    public static List<ValidationError> combine(List<List<ValidationError>> validationErrors) {
        return validationErrors.stream()
                     .flatMap(Collection::stream)
                     .distinct()
                     .collect(Collectors.toList());
    }
}
