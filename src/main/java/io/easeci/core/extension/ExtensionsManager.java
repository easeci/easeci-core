package io.easeci.core.extension;

import io.easeci.utils.io.YamlUtils;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.easeci.core.workspace.LocationUtils.getPluginsYmlLocation;
import static java.util.Objects.isNull;

@Slf4j
class ExtensionsManager {
    private static ExtensionsManager extensionsManager;

    private Path pluginYml;
    private InfrastructureInit infrastructureInit;

    @Getter
    private PluginContainer pluginContainer;
    private PluginResolver pluginResolver;
    private PluginLoader pluginLoader;

    private PluginDownloader pluginDownloader;

    private ExtensionsManager() {
        log.info("==> ExtensionManager instance creation process invoked");
        this.pluginYml = getPluginsYmlLocation();
        this.infrastructureInit = this.instantiateExtensionInfrastructure();
        this.pluginContainer = new DefaultPluginContainer();
        this.pluginResolver = new DefaultPluginResolver();
        this.pluginLoader = new DefaultPluginLoader(this.pluginContainer);
        this.pluginDownloader = this.instantiatePluginDownloader();
    }

    private InfrastructureInit instantiateExtensionInfrastructure() {
        InfrastructureInit infrastructureInit = new ExtensionInfrastructureInit();
        try {
            infrastructureInit.prepareInfrastructure();
            infrastructureInit.loadInfrastructure();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return infrastructureInit;
    }

    @SneakyThrows
    private PluginDownloader instantiatePluginDownloader() {
        Map<?, ?> yamlValues = YamlUtils.ymlLoad(getPluginsYmlLocation());

        Integer timeoutMilliseconds = (Integer) YamlUtils.ymlGet(yamlValues, "plugins.registry.timeout").getValue();
        URL registryUrl = new URL((String) YamlUtils.ymlGet(yamlValues, "plugins.registry.url").getValue());
        Path pluginDestination = this.infrastructureInit.getPluginDirectories().stream().findFirst().orElseThrow();

        return new DefaultPluginDownloader(registryUrl, pluginDestination, timeoutMilliseconds);
    }

    static ExtensionsManager getInstance() {
        if (isNull(extensionsManager)) {
            extensionsManager = new ExtensionsManager();
        }
        return extensionsManager;
    }

    void enableExtensions() {
        log.info("==> Declared plugins enabling started");
        Set<Plugin> resolve = pluginResolver.resolve(pluginYml, infrastructureInit);
        Set<Plugin> pluginsNotResolved = pluginLoader.loadPlugins(resolve);
        if (!pluginsNotResolved.isEmpty() && isDownloadProcessEnabled()) {
            download(pluginsNotResolved);
        } else if (pluginsNotResolved.isEmpty()) {
            log.info("====> All plugins was loaded correctly.\nReport:\n {}", getReport(resolve));
        }
    }

    private String getReport(Set<Plugin> resolve) {
        return resolve.stream()
                .map(Plugin::toString)
                .collect(Collectors.joining());
    }

    private boolean isDownloadProcessEnabled() {
        Path pluginsYmlLocation = getPluginsYmlLocation();
        return (Boolean) YamlUtils.ymlGet(pluginsYmlLocation, "plugins.local.download").getValue();
    }

    private void download(Set<Plugin> pluginSet) {
        log.info("===> Downloading of plugins just started for items: {}", getReport(pluginSet));
        pluginSet.stream()
                .filter(Plugin::isDownloadable)
                .filter(plugin -> !plugin.getJarArchive().isStoredLocally())
                .map(plugin -> pluginDownloader.download(plugin))
                .forEach(future -> future.whenComplete((plugin, throwable) -> {
                    Plugin pluginJustLoaded = pluginLoader.loadPlugin(plugin);
                    log.info("===> Downloading of plugin: {}\n just finished", pluginJustLoaded.toString());
                }));
    }
}
