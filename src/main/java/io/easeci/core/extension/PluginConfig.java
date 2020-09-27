package io.easeci.core.extension;

import java.util.UUID;

/**
 * This interface was created in order to control configuration
 * of plugins file in one place.
 * File to manage by this instance: plugins-config.json
 * Nowhere else this file should not be modify or change!
 * @author Karol Meksuła
 * 2020-04-06
 * */
interface PluginConfig {

    PluginsConfigFile load() throws PluginSystemCriticalException;

    /**
     * This method saves and reloading content to file.
     * After save application process will load file's content itself.
     * @return PluginConfigFile is an object with parsed information
     *          from file plugins-config.json
     *          It return new, fresh object with reloaded, updated information.
     * @throws PluginSystemCriticalException is throwing when loading
     *          of new content was not went with success.
     * */
    PluginsConfigFile save() throws PluginSystemCriticalException;

    /**
     * Use this method to add configuration of new plugin.
     * After invoke this method, configuration will be added
     * and saved to file plugins-config.json
     * Thanks for that we can persist some state of plugins in system.
     * @return boolean value that indicates if action was successful
     *          true - when everything went successfully
     *          false - when something went wrong and new configuration was not added to file
     * */
    boolean add(String interfaceName, ConfigDescription configDescription);

    /**
     * Use this method to change state for some plugin.
     * After invocation of this method, plugin with UUID typed in method argument
     * must be enabled in plugins-config.json
     * @param pluginUuid is an UUID of plugin from plugins-config.json file
     * @return boolean value that indicates if action was successful
     *          true - when everything went successfully
     *          false - when something went wrong and plugin was not enabled
     * */
    boolean enable(UUID pluginUuid);

    /**
     * In contrast to enable(), this method is created to disable plugin.
     * After invocation of this method plugin must be disabled and not running.
     * After restart of application this plugin will be not loaded.
     * @param pluginUuid is an UUID of plugin from plugins-config.json file
     * @return boolean value that indicates if action was successful
     *          true - when everything went successfully
     *          false - when something went wrong and plugin was not disabled
     * */
    boolean disable(UUID pluginUuid);

    /**
     * In contrast to enable(), this method is created to disable plugin.
     * After invocation of this method plugin must be disabled and not running.
     * After restart of application this plugin will be not loaded.
     * @param pluginName is a name of plugin from plugins-config.json file
     * @param pluginVersion is a version of plugin from plugins-config.json file
     * @return boolean value that indicates if action was successful
     *          true - when everything went successfully
     *          false - when something went wrong and plugin was not disabled
     * Notice that pluginName and pluginVersion must be compatible.
     * */
    boolean disable(String pluginName, String pluginVersion);
}
