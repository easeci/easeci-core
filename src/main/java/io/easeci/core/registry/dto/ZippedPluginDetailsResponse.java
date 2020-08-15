package io.easeci.core.registry.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.easeci.core.extension.PluginState;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "of")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ZippedPluginDetailsResponse {
    private PluginDetailsResponse registryPluginDetails;
    private PluginState state;
}
