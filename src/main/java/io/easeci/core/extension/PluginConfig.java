package io.easeci.core.extension;

import java.util.UUID;

interface PluginConfig {

    PluginsConfigFile load();

    PluginsConfigFile save();

    boolean add(String interfaceName, ConfigDescription configDescription);

    boolean update(ConfigDescription configDescription);

    boolean remove(ConfigDescription configDescription);

    boolean enable(UUID pluginUuid);

    boolean disable(UUID pluginUuid);

    boolean disable(String pluginName, String pluginVersion);

    boolean disableAll(String interfaceName);
}
