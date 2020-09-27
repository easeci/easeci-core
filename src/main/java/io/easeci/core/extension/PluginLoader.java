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
     * Use this method in order to shut down and create new instance of specified plugin.
     * Whole initialization process will be execute again, new object created by reflection etc.
     * Old instance should be destroyed by Garbage Collector
     * when there is not references to this object but this is not guaranteed by EaseCI Core.
     * @param instance is a object obtained from PluginContainer. That hold reference to plugin's
     *                 entry object and important information about plugin in system.
     *                 You need to provide this one to recreate/reinstantiate this.
     * @param pluginStrategy is object required for checking if plugin is enabled or not.
     *                       PluginStrategy has information about current plugin state
     *                       that is persisted in plugins-config.json file.
     * */
    Instance reinstantiatePlugin(Instance instance, PluginStrategy pluginStrategy);
}
