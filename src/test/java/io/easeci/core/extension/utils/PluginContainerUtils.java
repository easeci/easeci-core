package io.easeci.core.extension.utils;

import io.easeci.core.extension.ExtensionManifest;
import io.easeci.core.extension.Instance;
import io.easeci.core.extension.Plugin;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

public class PluginContainerUtils {

    public static Instance fromBasic(String interfaceName, Object implementation) {
        Plugin plugin = Plugin.of("test", "0.0.1");
        ExtensionManifest manifest = ExtensionManifest.of(interfaceName, "java.lang.String");
        Plugin.JarArchive jar = Plugin.JarArchive.of(null, false, null, null, manifest);
        return Instance.builder()
                .plugin(Plugin.of(plugin, jar))
                .instance(implementation)
                .build();
    }

    public static Instance fromBasic(String interfaceName, Object implementation, String version) {
        Plugin plugin = Plugin.of("test", version);
        ExtensionManifest manifest = ExtensionManifest.of(interfaceName, "java.lang.String");
        Plugin.JarArchive jar = Plugin.JarArchive.of(null, false, null, null, manifest);
        return Instance.builder()
                .plugin(Plugin.of(plugin, jar))
                .instance(implementation)
                .build();
    }

    public static Instance fromBasicWithPluginName(String interfaceName, Object implementation, String name, String version) {
        Plugin plugin = Plugin.of(name, version);
        ExtensionManifest manifest = ExtensionManifest.of(interfaceName, "java.lang.String");
        Plugin.JarArchive jar = Plugin.JarArchive.of(null, false, null, null, manifest);
        return Instance.builder()
                .plugin(Plugin.of(plugin, jar))
                .instance(implementation)
                .build();
    }

    public static Instance fromBasic(String interfaceName, Object implementation, String pluginName, String pluginVersion) {
        Plugin plugin = Plugin.of(pluginName, pluginVersion);
        ExtensionManifest manifest = ExtensionManifest.of(interfaceName, "java.lang.String");
        Plugin.JarArchive jar = Plugin.JarArchive.of(null, false, null, null, manifest);
        return Instance.builder()
                .plugin(Plugin.of(plugin, jar))
                .instance(implementation)
                .build();
    }

    /**
     * Plugin correlated with data from plugins-config-test-all-disabled.json
     * */
    public static Plugin createFakePlugin() {
        Plugin.JarArchive jarArchive = null;
        try {
            jarArchive = Plugin.JarArchive.of("welcome-logo-0.0.2.jar",
                                                true,
                                                new URL("file://"),
                                                Path.of(""),
                                                ExtensionManifest.of("", ""));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Plugin plugin = Plugin.of("welcome-logo", "0.0.2");
        return Plugin.of(plugin, jarArchive);
    }
}
