package io.easeci.api.extension;

import io.easeci.extension.ExtensionType;
import lombok.*;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ActionRequest {

    private ExtensionType extensionType;

    @Size(min = 1, max = 100)
    private String pluginName;

    @Size(min = 1, max = 18)
    @Pattern(regexp = "^((([0-9]+)\\.([0-9]+)\\.([0-9]+)(?:-([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?)(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?)$")
    private String pluginVersion;

    private UUID pluginUuid;
}
