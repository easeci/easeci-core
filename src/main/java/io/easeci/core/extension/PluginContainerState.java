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