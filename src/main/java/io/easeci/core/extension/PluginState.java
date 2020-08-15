package io.easeci.core.extension;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.easeci.extension.ExtensionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PluginState {
    private String implementsValue;
    private String pluginName;
    private String pluginVersion;
    private ExtensionType extensionType;
    private boolean isRunning;
    private String runDate;
    private ConfigDescription configDescription;
}
