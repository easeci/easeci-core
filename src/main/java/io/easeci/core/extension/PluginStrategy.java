package io.easeci.core.extension;

import io.easeci.extension.ExtensionType;

import java.util.List;
import java.util.UUID;

interface PluginStrategy {
    Instance choose(List<Instance> instanceList, String interfaceName);

    PluginsConfigFile pluginsConfigFile();

    ConfigDescription find(ExtensionType extensionType, String pluginName, String pluginVersion);

    ConfigDescription find(ExtensionType extensionType, UUID uuid);
}
