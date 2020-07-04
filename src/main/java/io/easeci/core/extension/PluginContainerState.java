package io.easeci.core.extension;

import io.easeci.extension.ExtensionType;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PluginContainerState {
    private List<PluginState> pluginStates;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class PluginState {
    private String implementsValue;
    private String pluginName;
    private String pluginVersion;
    private ExtensionType extensionType;
    private boolean isRunning;
    private String runDate;
    private ConfigDescription configDescription;
}

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
class PluginStateProxy {
    private String implementsValue;
    private Instance instance;

    PluginState toPluginState(PluginStrategy pluginStrategy) {
        ExtensionType extensionType = ExtensionType.toEnum(this.implementsValue);
        String pluginName = this.instance.getPlugin().getName();
        String pluginVersion = this.instance.getPlugin().getVersion();
        return PluginState.builder()
                .implementsValue(this.implementsValue)
                .pluginName(pluginName)
                .pluginVersion(pluginVersion)
                .extensionType(extensionType)
                .isRunning(this.instance.isRunning())   // TODO ping method needs to implements
                .runDate(this.instance.getInstantiateDateTime().toString())
                .configDescription(pluginStrategy.find(extensionType, pluginName, pluginVersion))
                .build();
    }
}