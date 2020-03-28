package io.easeci.core.extension;

import java.nio.file.Path;
import java.util.Set;

/**
 * Functional interface of object that should resolve
 * plugin required in application runtime, declared
 * in plugins.yml file.
 * @author Karol Meksuła
 * 2020-03-21
 * */
interface PluginResolver {

    /**
     * Simply parse plugins.yml file and resolve all dependencies
     * expressed as plugins.
     * @param pluginYml is a path to plugins.yml file on your local
     *                  storage that will be parsed.
     * @param infrastructureInit is object that should provide
     *                           information about directories where
     *                           plugins should be stored on local storage.
     * @return Set<Plugin> is collection of plugin's
     *          jar files required in EaseCI core runtime.
     * */
    Set<Plugin> resolve(Path pluginYml, InfrastructureInit infrastructureInit);
}
