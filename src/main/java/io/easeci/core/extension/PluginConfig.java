package io.easeci.core.extension;

interface PluginConfig {

    PluginsConfigFile load();

    PluginsConfigFile save(PluginsConfigFile pluginsConfigFile);
}
