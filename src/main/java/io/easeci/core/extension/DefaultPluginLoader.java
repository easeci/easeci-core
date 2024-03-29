package io.easeci.core.extension;

import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class DefaultPluginLoader implements PluginLoader {
    private PluginContainer pluginContainer;
    private ManifestReader manifestReader;

    DefaultPluginLoader(PluginContainer pluginContainer, ManifestReader manifestReader) {
        if (isNull(pluginContainer)) {
            throw new IllegalStateException("Cannot construct PluginLoader implementation with not initialized PluginContainer!");
        }
        this.pluginContainer = pluginContainer;
        this.manifestReader = manifestReader;
    }

    @Override
    public Set<Plugin> loadPlugins(Set<Plugin> pluginSetInput, PluginStrategy pluginStrategy) {
        Set<Plugin> pluginSetOutput = pluginSetInput.stream()
                .filter(Plugin::isLoadable)
                .map(manifestReader::attachManifest)
                .peek(plugin -> instantiatePlugin(plugin, pluginStrategy))
                .collect(Collectors.toSet());
        return new HashSet<>(Sets.difference(pluginSetInput, pluginSetOutput));
    }

    @Override
    public Instance reinstantiatePlugin(Instance instance, PluginStrategy pluginStrategy) {
        Plugin plugin = manifestReader.attachManifest(instance.getPlugin());
        Object inst = this.instantiate(plugin);
        boolean isRemoved = pluginContainer.remove(plugin.getName(), plugin.getVersion());
        this.insert(plugin, inst);
        return pluginContainer.findByIdentityHashCode(System.identityHashCode(inst)).orElseThrow();
    }

    /**
     * Checks by PluginStrategy if plugin is correctly defined in plugins-config.json file.
     * If configuration is correct then create object and insert to container.
     * */
    private void instantiatePlugin(Plugin plugin, PluginStrategy pluginStrategy) {
        Boolean isEnabled = pluginStrategy.find(plugin.getName(), plugin.getVersion()).getEnabled();
        if (isEnabled) {
            Object instance = this.instantiate(plugin);
            this.insert(plugin, instance);
        } else {
            this.insert(plugin, null);
        }
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
                .instantiateDateTime(LocalDateTime.now())
                .build();
        this.pluginContainer.add(instance);
    }
}
