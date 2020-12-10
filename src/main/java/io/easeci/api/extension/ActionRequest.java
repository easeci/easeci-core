package io.easeci.api.extension;

import io.easeci.api.validation.ValidationError;
import io.easeci.api.validation.Validator;
import io.easeci.extension.ExtensionType;
import lombok.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static io.easeci.api.validation.CommonValidatorSet.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ActionRequest implements Validator {

    private ExtensionType extensionType;

    private String pluginName;

    private String pluginVersion;

    private UUID pluginUuid;

    @Override
    public List<ValidationError> validate() {
        final String versionRegexp = "^((([0-9]+)\\.([0-9]+)\\.([0-9]+)(?:-([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?)(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?)$";
        return combine(
                Arrays.asList(
                        validateStringLength(this.pluginName, 1, 100, "pluginName", true),
                        validateStringLength(this.pluginVersion, 1, 18, "pluginVersion", true),
                        validateStringPattern(this.pluginVersion, versionRegexp, "pluginVersion", true),
                        validateUuid(this.pluginUuid, "pluginUuid", true)
                )
        );
    }
}
