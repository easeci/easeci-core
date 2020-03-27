package io.easeci.core.extension;

import lombok.AllArgsConstructor;

import java.util.Set;

@AllArgsConstructor
class DefaultPluginLoader implements PluginLoader {
    private PluginContainer pluginContainer;

    Set<Plugin> loadPlugins(Set<Plugin> pluginSet) {

    }
}
