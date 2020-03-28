package io.easeci.core.extension;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static io.easeci.core.workspace.LocationUtils.getPluginsYmlLocation;
import static java.util.Objects.isNull;

@Slf4j
class ExtensionsManager {
    private static ExtensionsManager extensionsManager;

    private Path pluginYml;
    private InfrastructureInit infrastructureInit;
    private PluginContainer pluginContainer;
    private PluginResolver pluginResolver;
    private PluginLoader pluginLoader;

    private PluginDownloader pluginDownloader;

    private ExtensionsManager() {
        log.info("==> ExtensionManager instance creation process invoked");
        this.pluginYml = getPluginsYmlLocation();
        this.infrastructureInit = new ExtensionInfrastructureInit();
        this.pluginContainer = new DefaultPluginContainer();
        this.pluginResolver = new DefaultPluginResolver();
        this.pluginLoader = new DefaultPluginLoader(this.pluginContainer);
    }

    static ExtensionsManager getInstance() {
        if (isNull(extensionsManager)) {
            extensionsManager = new ExtensionsManager();
        }
        return extensionsManager;
    }

    void enableExtensions() {
        Set<Plugin> resolve = pluginResolver.resolve(pluginYml, infrastructureInit);
        Set<Plugin> plugins = pluginLoader.loadPlugins(resolve);
    }

    Set<CompletableFuture<Plugin>> download(Set<Plugin> pluginSet) {
        return pluginSet.stream()
                .map(plugin -> pluginDownloader.download(plugin))
                .collect(Collectors.toSet());
    }
}
