package io.easeci.core.extension;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Joins external .jar files and connect it with current application process
 */
@Slf4j
class JarJoiner {

    Plugin addToClasspath(Plugin plugin) {
        if (!plugin.isLoadable()) {
            log.info("===> Plugin {}, v{} is missing on local storage", plugin.getName(), plugin.getVersion());
            return plugin;
        }
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
    }

    ExtensionManifest read(Plugin plugin) {
        try {
            ExtensionManifest extensionManifest = Utils.extractManifest(plugin.getJarArchive().getJarPath());
            if (extensionManifest.isComplete()) {
                return extensionManifest;
            }
        } catch (IOException e) {
            log.error("IOException occurred while trying to read jar file's manifest. Check values for plugin:\n" + plugin.toString());
        }
        throw new ExtensionManifestException("ExtensionManifest is not correctly initialized for plugin:\n" + plugin.toString());
    }
}
