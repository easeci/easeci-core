package io.easeci.core.extension;

import io.easeci.extension.ExtensionType;

import java.util.List;
import java.util.UUID;

/**
 * Instance of this object should provide information
 * which plugin should we use in specific case.
 * We shouldn't refer directly to PluginContainer,
 * but we should refer to PluginStrategy when we want to obtain reference to some plugin.
 * Why? Because PluginStrategy knows what reference should we receive.
 * @author Karol Meksuła
 * 2020-04-06
 * */
interface PluginStrategy {

    /**
     * In container we can storage many of plugin of some specific type,
     * but only one should be enabled.
     * Consider case, when we want to receive plugin to clone git repository,
     * but we have installed few plugin of the same type: gitlab, github and bitbucket.
     * What next? Which plugin should be invoked? This plugin return as object that we need to.
     * @param instanceList is list of instances of such interface type from PluginContainer.
     * @param interfaceName is a name of interface exposed in domain module that we can extend.
     * @return instance representation of specific instance that was chosen by algorithm.
     * */
    Instance choose(List<Instance> instanceList, String interfaceName);

    /**
     * Use it to receive dump of PluginConfigFile.
     * @return PluginConfigFile that is 1:1 dump of plugins-config.json as POJO object
     * */
    PluginsConfigFile pluginsConfigFile();

    /**
     * Use this method when you want to track down plugin and receive its config.
     * @param extensionType specify type
     * @param pluginName is a name of plugin same as in plugins-config.json file
     * @param pluginVersion is a version of plugin same as in plugins-config.json file
     * @return ConfigDescription is POJO representation of plugin stored in plugins-config.json
     * */
    ConfigDescription find(ExtensionType extensionType, String pluginName, String pluginVersion);

    /**
     * Use this method when you want to track down plugin and receive its config.
     * @param extensionType specify type
     * @param uuid is a UUID of plugin same as in plugins-config.json file
     * @return ConfigDescription is POJO representation of plugin stored in plugins-config.json
     * */
    ConfigDescription find(ExtensionType extensionType, UUID uuid);

    /**
     * Use this method when you want to track down plugin and receive its config.
     * @param pluginName is a name of plugin same as in plugins-config.json file
     * @param pluginVersion is a version of plugin same as in plugins-config.json file
     * @return ConfigDescription is POJO representation of plugin stored in plugins-config.json
     * Notice that this method is slower than find(ExtensionType extensionType, String pluginName, String pluginVersion)
     * because it is searching in whole set of plugin's config.
     * */
    ConfigDescription find(String pluginName, String pluginVersion);
}
