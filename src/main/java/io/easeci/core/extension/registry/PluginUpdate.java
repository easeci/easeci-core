package io.easeci.core.extension.registry;

import io.easeci.core.extension.registry.dto.PluginUpdateCheckResponse;
import ratpack.exec.Promise;

public interface PluginUpdate {

    Promise<PluginUpdateCheckResponse> checkForUpdate(String pluginName, String pluginVersion);
}
