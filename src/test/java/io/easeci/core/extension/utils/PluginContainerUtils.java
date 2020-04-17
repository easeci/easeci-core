package io.easeci.core.extension.utils;

import io.easeci.core.extension.ExtensionManifest;
import io.easeci.core.extension.Instance;
import io.easeci.core.extension.Plugin;

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

    public static Instance fromBasic(String interfaceName, Object implementation, String pluginName, String pluginVersion) {
        Plugin plugin = Plugin.of(pluginName, pluginVersion);
        ExtensionManifest manifest = ExtensionManifest.of(interfaceName, "java.lang.String");
        Plugin.JarArchive jar = Plugin.JarArchive.of(null, false, null, null, manifest);
        return Instance.builder()
                .plugin(Plugin.of(plugin, jar))
                .instance(implementation)
                .build();
    }
}
