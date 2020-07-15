package io.easeci.core.extension;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Defines a method for downloading plugin .jar files from
 * remote repository.
 * @author Karol Meksuła
 * 2020-03-28
 * */
abstract class PluginDownloader {

    /**
     * Download plugin from remote server.
     * Concrete implementation of this abstract method should download
     * plugin and fill missing fields in Plugin.class object.
     * Downloaded jar file will be stored in specified location and this
     * paths should be placed in Plugin.class instance.
     * @param plugin specify what plugin is required to download
     * @return CompletableFuture just wrapping Plugin that is
     *          just downloading asynchronously.
     * */
    abstract CompletableFuture<Plugin> download(Plugin plugin);

    /**
     * Download defined set of plugin from remote server.
     * @param pluginSet specify what plugins are required to download
     * @return CompletableFuture just wrapping Plugin that is
     *          just downloading asynchronously.
     * */
    abstract Stream<CompletableFuture<Plugin>> download(Set<Plugin> pluginSet);
}
