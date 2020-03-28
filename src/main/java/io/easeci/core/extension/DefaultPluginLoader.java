package io.easeci.core.extension;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class DefaultPluginLoader implements PluginLoader {
    private PluginContainer pluginContainer;

    DefaultPluginLoader(PluginContainer pluginContainer) {
        if (isNull(pluginContainer)) {
            throw new IllegalStateException("Cannot construct PluginLoader implementation with not initialized PluginContainer!");
        }
        this.pluginContainer = pluginContainer;
    }

    @Override
    public Set<Plugin> loadPlugins(Set<Plugin> pluginSet) {
        return pluginSet.stream()
                .filter(Plugin::isLoadable)
                .map(plugin -> {
                    try {
                        URLClassLoader urlClassLoader = new URLClassLoader(new URL[0]);
                        Method addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                        addUrlMethod.setAccessible(true);
                        addUrlMethod.invoke(urlClassLoader, plugin.getJarArchive().getJarUrl());
                        ExtensionManifest extensionManifest = read(plugin);
                        plugin.getJarArchive().setExtensionManifest(extensionManifest);
                        return plugin;
                    } catch (NoSuchMethodException e) {
                        log.error("NoSuchMethodException occurred: {}", e.getMessage());
                    } catch (IllegalAccessException e) {
                        log.error("IllegalAccessException occurred: {}", e.getMessage());
                    } catch (InvocationTargetException e) {
                        log.error("InvocationTargetException occurred: {}", e.getMessage());
                    }
                    return plugin;
                }).peek(plugin -> {
                    Object instance = instantiate(plugin);
                    this.insert(plugin, instance);
                })
                .collect(Collectors.toSet());
    }

    ExtensionManifest read(Plugin plugin) {
        try {
            JarFile jarFile = new JarFile(plugin.getJarArchive().getJarPath().toFile());
            Manifest manifest = jarFile.getManifest();
            Attributes mainAttributes = manifest.getMainAttributes();
            ExtensionManifest extensionManifest = ExtensionManifest.of(mainAttributes);
            if (extensionManifest.isComplete()) {
                return extensionManifest;
            }
        } catch (IOException e) {
            log.error("IOException occurred while trying to read jar file's manifest. Check values for plugin:\n" + plugin.toString());
        }
        throw new ExtensionManifestException("ExtensionManifest is not correctly initialized for plugin:\n" + plugin.toString());
    }

    Object instantiate(Plugin plugin) {
        return new ReflectiveFactory.ReflectiveFactoryBuilder<>()
                .classReference(plugin.getJarArchive().getExtensionManifest().getEntryClassProperty())
                .build()
                .instantiate();
    }

    void insert(Plugin plugin, Object instance) {
        this.pluginContainer.add(plugin.getJarArchive().getExtensionManifest().getImplementsProperty(), instance);
    }
}
