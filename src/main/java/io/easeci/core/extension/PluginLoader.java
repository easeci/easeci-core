package io.easeci.core.extension;

import java.util.Set;

/**
 * An instance of this class is responsible for loading .jar files
 * into ClassPath, and then appoints new instances of classes
 * implementing the API contained in the extension-api project
 * @author Karol Meksuła
 * 2020-03-28
 * */
interface PluginLoader {

    /**
     * Main method of this interface.
     * This should loads plugin in custom way.
     * @param pluginSet is set of plugin, provided read from some source.
     *                  All plugins in this set will be loaded or
     *                  if some plugin was not loaded correctly, one should be returned.
     * @param pluginStrategy is object required for checking if plugin is enabled or not.
     *                       PluginStrategy has information about current plugin state
     *                       that is persisted in plugins-config.json file.
     * @return Set<Plugin> that should be set of plugins
     *          that was NOT correctly loaded and NOT placed in classpath
     *          for some reason.
     * */
    Set<Plugin> loadPlugins(Set<Plugin> pluginSet, PluginStrategy pluginStrategy);

    /**
     * Main method of this interface.
     * This should loads plugin in custom way.
     * @param plugin is a plugin representation, provided read from some source.
     *                  Plugins will be loaded or if it was not loaded correctly,
     *                  one should be returned.
     * @return Plugin that should be plugin representation
     *          that was NOT correctly loaded and NOT placed in classpath
     *          for some reason.
     * */
    Plugin loadPlugin(Plugin plugin);
}
