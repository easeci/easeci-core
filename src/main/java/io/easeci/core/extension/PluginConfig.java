package io.easeci.core.extension;

import java.util.UUID;

interface PluginConfig {

    PluginsConfigFile load();

    PluginsConfigFile save(PluginsConfigFile pluginsConfigFile);

    boolean enable(UUID pluginUuid);

    boolean disableAll(String interfaceName);
}
