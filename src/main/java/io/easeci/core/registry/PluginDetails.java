package io.easeci.core.registry;

import io.easeci.core.registry.dto.ZippedPluginDetailsResponse;
import ratpack.exec.Promise;

public interface PluginDetails {

    Promise<ZippedPluginDetailsResponse> fetchDetails(String pluginName, String pluginVersion);
}
