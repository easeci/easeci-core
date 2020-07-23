package io.easeci.core.extension;

/**
 * Main point of resolving issues with plugins integrity
 * in extension package.
 * Instance of this interface should be able to fix problems
 * for instance with malformed plugins declaration etc.
 * What problem could PluginSystemIntegrityFixer solve?
 * For instance: EaseCI core just downloaded plugin and
 * process unexpectedly was killed. Then, new configuration
 * was not saved correctly to file. After restart EaseCI core
 * we will receive PluginSystemCriticalException.class at startup,
 * so application could not run.
 * In that case PluginSystemIntegrityFixer should be run after
 * exception thrown and fix this discordant in configuration files.
 * @author Karol Meksuła
 * 2020-07-23
 * */
public interface PluginSystemIntegrityFixer {

    /**
     * Fixes discordant between plugins.yml and plugins-config.json files.
     * @param pluginsFile is a POJO representation of text configuration file.
     * @param pluginsConfigFile is a POJO representation of text configuration file.
     * @return report of issue fixing as a String representation
     * */
    String fixPluginsConfigFiles(PluginsFile pluginsFile, PluginsConfigFile pluginsConfigFile);
}
