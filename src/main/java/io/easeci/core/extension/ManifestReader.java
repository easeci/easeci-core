package io.easeci.core.extension;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelName.PLUGIN_EVENT;
import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelPrefix.THREE;
import static io.easeci.core.log.ApplicationLevelLogFacade.logit;

/**
 * Joins external .jar files and connect it with current application process
 */
@Slf4j
class ManifestReader {

    Plugin attachManifest(Plugin plugin) {
        if (!plugin.isLoadable()) {
            logit(PLUGIN_EVENT, "Plugin " + plugin.getName() + ", v" + plugin.getVersion() + " is missing on local storage", THREE);
            return plugin;
        }
        ExtensionManifest extensionManifest = read(plugin);
            plugin.getJarArchive().setExtensionManifest(extensionManifest);
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
