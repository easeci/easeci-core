package io.easeci.core.extension;

import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class DefaultPluginLoader implements PluginLoader {
    private PluginContainer pluginContainer;
    private JarJoiner jarJoiner;

    DefaultPluginLoader(PluginContainer pluginContainer, JarJoiner jarJoiner) {
        if (isNull(pluginContainer)) {
            throw new IllegalStateException("Cannot construct PluginLoader implementation with not initialized PluginContainer!");
        }
        this.pluginContainer = pluginContainer;
        this.jarJoiner = jarJoiner;
    }

    @Override
    public Set<Plugin> loadPlugins(Set<Plugin> pluginSetInput, PluginStrategy pluginStrategy) {
        Set<Plugin> pluginSetOutput = pluginSetInput.stream()
                .filter(Plugin::isLoadable)
                .map(jarJoiner::addToClasspath)
                .peek(plugin -> {
                    Boolean isEnabled = pluginStrategy.find(plugin.getName(), plugin.getVersion()).getEnabled();
                    if (isEnabled) {
                        Object instance = this.instantiate(plugin);
                        this.insert(plugin, instance);
                    } else {
                        this.insert(plugin, null);
                    }
                }).collect(Collectors.toSet());
        return new HashSet<>(Sets.difference(pluginSetInput, pluginSetOutput));
    }

    @Override
    public Plugin loadPlugin(Plugin plugin) {
        if (!plugin.isLoadable()) {
            log.error("===> {} is not loadable! Basic information required for plugin load was not provided.", plugin);
        }
        Plugin pluginAdded = jarJoiner.addToClasspath(plugin);
        Object instance = instantiate(pluginAdded);
        this.insert(plugin, instance);
        return pluginAdded;
    }

    Object instantiate(Plugin plugin) {
        return new ReflectiveFactory.ReflectiveFactoryBuilder<>()
                .classReference(plugin.getJarArchive().getExtensionManifest().getEntryClassProperty())
                .build()
                .instantiate(plugin.getJarArchive());
    }

    void insert(Plugin plugin, Object object) {
        Instance instance = Instance.builder()
                .plugin(plugin)
                .instance(object)
                .identityHashCode(nonNull(object) ? System.identityHashCode(object) : 0)
                .build();
        this.pluginContainer.add(instance);
    }
}
