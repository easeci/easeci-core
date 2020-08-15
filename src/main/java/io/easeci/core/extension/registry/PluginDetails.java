package io.easeci.core.extension.registry;

import io.easeci.core.extension.registry.dto.ZippedPluginDetailsResponse;
import ratpack.exec.Promise;

public interface PluginDetails {

    Promise<ZippedPluginDetailsResponse> fetchDetails(String pluginName, String pluginVersion);
}
