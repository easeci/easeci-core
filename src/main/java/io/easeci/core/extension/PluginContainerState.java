package io.easeci.core.extension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.easeci.extension.ExtensionType;
import io.easeci.extension.State;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PluginContainerState {
//    private Map<ExtensionType, Map<String, PluginState>> containerState;

    private Map<String, List<Instance>> container;
    private PluginsConfigFile pluginsConfigFile;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
class PluginState extends State {
    private String pluginName;
    private String pluginVersion;
}
