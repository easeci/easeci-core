package io.easeci.core.extension;

import io.easeci.api.extension.ActionRequest;
import io.easeci.api.extension.ActionResponse;
import io.easeci.extension.ExtensionType;
import io.easeci.commons.YamlUtils;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.easeci.core.workspace.LocationUtils.getPluginsYmlLocation;
import static java.util.Objects.isNull;

@Slf4j
class ExtensionsManager implements ExtensionControllable {
    private static ExtensionsManager extensionsManager;

    private Path pluginYml;
    private InfrastructureInit infrastructureInit;

    @Getter
    private PluginContainer pluginContainer;
    private PluginResolver pluginResolver;
    private PluginLoader pluginLoader;
    private PluginConfig pluginConfig;

    private PluginDownloader pluginDownloader;

    private ExtensionsManager(Path pluginYml, Path pluginConfigYml) {
        log.info("==> ExtensionManager instance creation process invoked");
        this.pluginYml = pluginYml;
        this.infrastructureInit = this.instantiateExtensionInfrastructure();
        this.pluginConfig = new DefaultPluginConfig(pluginConfigYml);
        this.pluginContainer = new DefaultPluginContainer((PluginStrategy) pluginConfig);
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

    static ExtensionsManager getInstance(Path pluginYml, Path pluginConfigYml) {
        if (isNull(extensionsManager)) {
            extensionsManager = new ExtensionsManager(pluginYml, pluginConfigYml);
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

    @Override
    public PluginContainerState state(ExtensionType extensionType) {
        return this.pluginContainer.state(extensionType);
    }

    @Override
    public ActionResponse shutdownExtension(ActionRequest actionRequest) {
        log.info("===> Trying to finish plugin identified by UUID: " + actionRequest.getPluginUuid());
        return pluginContainer.findByUuid(actionRequest.getExtensionType(), actionRequest.getPluginUuid())
                .map(instance -> zip(
                        Stream.of(interruptPluginThread(), modifyConfigFile(), reloadContainer())
                                .map(func -> func.apply(instance))
                                .collect(Collectors.toList()))
                ).orElseGet(() -> ActionResponse.builder()
                        .isSuccessfullyDone(false)
                        .message("Cannot find plugin to shutdown: ".concat(actionRequest.toString()))
                        .build());
    }

    private Function<Instance, ActionResponse> interruptPluginThread() {
        return instance -> {
            instance.getThread().interrupt();
            boolean isInterrupted = instance.getThread().isInterrupted();
            if (isInterrupted) {
                return ActionResponse.of(true,
                        List.of("Thread ".concat(instance.thread.getName()).concat(" is interrupted. Waiting for gently kill it.")));
            }
            return ActionResponse.of(false,
                    List.of("Thread ".concat(instance.thread.getName())
                            .concat(" is not interrupted yet, but interrupt() method was called. " +
                                    "Waiting for gently kill it. Check is this thread is alive again.")));
        };
    }

    private Function<Instance, ActionResponse> onPluginFinish() {
        return instance -> null;
    }

    private Function<Instance, ActionResponse> modifyConfigFile() {
        return instance -> ActionResponse.of(true, List.of("Correct"));  // TODO
    }

    private Function<Instance, ActionResponse> reloadContainer() {
        return instance -> ActionResponse.of(true, List.of("Correct"));  // TODO
    }

    private static ActionResponse zip(List<ActionResponse> actionResponseList) {
        return actionResponseList.stream()
                .reduce(((responseA, responseB) -> {
                    boolean isSuccess = Boolean.logicalAnd(responseA.getIsSuccessfullyDone(), responseB.getIsSuccessfullyDone());
                    List<String> messagesAggregated = Stream.of(responseB.getMessages(),
                            nullableToList(responseB.getMessage()),
                            responseA.getMessages(),
                            nullableToList(responseA.getMessage()))
                                .filter(Objects::nonNull)
                                .flatMap(Collection::stream)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                    return ActionResponse.of(isSuccess, messagesAggregated);
                })).orElseThrow();
    }

    private static List<String> nullableToList(String message) {
        return isNull(message) ? Collections.emptyList() : List.of(message);
    }

    @Override
    public ActionResponse startupExtension(ActionRequest actionRequest) {
//        TODO
        return null;
    }

    @Override
    public ActionResponse restart(ActionRequest actionRequest) {
//        TODO
        return null;
    }

    private String getReport(Set<Plugin> resolve) {
        return resolve.stream()
                .map(Plugin::toString)
                .collect(Collectors.joining("\n"));
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
