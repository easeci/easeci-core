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
import java.util.concurrent.CompletableFuture;
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

    private ExtensionsManager(Path pluginYml, Path pluginConfigYml) throws PluginSystemCriticalException {
        log.info("==> ExtensionManager instance creation process invoked");
        this.pluginYml = pluginYml;
        this.infrastructureInit = this.instantiateExtensionInfrastructure();
        this.pluginConfig = new DefaultPluginConfig(pluginConfigYml);
        this.pluginContainer = new DefaultPluginContainer((PluginStrategy) pluginConfig);
        this.pluginResolver = new DefaultPluginResolver();
        this.pluginLoader = new DefaultPluginLoader(this.pluginContainer, new JarJoiner());
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

        return DefaultPluginDownloader.builder()
                .registryUrl(registryUrl)
                .targetPath(pluginDestination)
                .timeoutMilliseconds(timeoutMilliseconds)
                .build();
    }

    static ExtensionsManager getInstance(Path pluginYml, Path pluginConfigYml) throws PluginSystemCriticalException {
        if (isNull(extensionsManager)) {
            extensionsManager = new ExtensionsManager(pluginYml, pluginConfigYml);
        }
        return extensionsManager;
    }

    void enableExtensions() {
        log.info("==> Declared plugins enabling started");
        Set<Plugin> resolvedPlugins = pluginResolver.resolve(pluginYml, infrastructureInit);
        Set<Plugin> pluginsNotResolved = pluginLoader.loadPlugins(resolvedPlugins, (PluginStrategy) pluginConfig);
        if (!pluginsNotResolved.isEmpty() && isDownloadProcessEnabled()) {
            Set<CompletableFuture<Plugin>> justDownloaded = download(pluginsNotResolved);
            justDownloaded.stream()
                    .map(future -> future.thenApply(plugin -> pluginLoader.loadPlugins(Set.of(plugin), (PluginStrategy) pluginConfig)))
                    .forEach(future -> future.thenAccept(pluginSet -> {
                        pluginSet.forEach(plugin -> log.info("===> Plugin {} correctly installed in EaseCI system", plugin.toShortString()));
                    }));
        } else if (pluginsNotResolved.isEmpty()) {
            log.info("====> All plugins was loaded correctly.\nReport:\n {}", getReport(resolvedPlugins));
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
                        Stream.of((instance.isStandalone() ? interruptStandalonePlugin() : interruptNotStandalonePlugin()), modifyConfigFile())
                                .map(func -> func.apply(instance))
                                .collect(Collectors.toList()))
                ).orElseGet(() -> ActionResponse.builder()
                        .isSuccessfullyDone(false)
                        .message("Cannot find plugin to shutdown: ".concat(actionRequest.toString()))
                        .build());
    }

    private Function<Instance, ActionResponse> interruptStandalonePlugin() {
        return instance -> {
            log.info("===> Stopping standalone plugin work " + instance.getPlugin().toShortString());
            instance.toStandalone().stop();
            boolean instanceCleared = instance.clear();
            if (instanceCleared) {
                return ActionResponse.of(true,
                        List.of("Plugin " + instance.getPlugin().toShortString() + " is stopped by stop() method"));
            }
            return ActionResponse.of(false,
                    List.of("Plugin " + instance.getPlugin().toShortString() + " was not stopped correctly"));
        };
    }

    private Function<Instance, ActionResponse> interruptNotStandalonePlugin() {
        return instance -> {
            log.info("===> Stopping other than standalone plugin work " + instance.getPlugin().toShortString());
            boolean instanceCleared = instance.clear();
            if (instanceCleared) {
                return ActionResponse.of(true,
                        List.of("Plugin " + instance.getPlugin().toShortString() + " should be removed from memory of JVM by GC"));
            }
            return ActionResponse.of(false,
                    List.of("Plugin " + instance.getPlugin().toShortString() + " was not removed"));
        };
    }

    private Function<Instance, ActionResponse> modifyConfigFile() {
        return instance -> {
            boolean isDisabled = pluginConfig.disable(instance.getPlugin().getName(), instance.getPlugin().getVersion());
            if (isDisabled) {
                return ActionResponse.of(true,
                        List.of("Config file was modified correctly, and plugin " + instance.getPlugin().toShortString() + " was disabled"));
            }
            return ActionResponse.of(false,
                    List.of("Config file was not modified"));
        };
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
//        TODO !! urgent !!
        return null;
    }

    @Override
    public ActionResponse restart(ActionRequest actionRequest) {
//        TODO
        return null;
    }

    public Optional<Instance> findInstanceByIdentityHashCode(int hashCode) {
        return this.pluginContainer.findByIdentityHashCode(hashCode);
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

    private Set<CompletableFuture<Plugin>> download(Set<Plugin> pluginSet) {
        log.info("===> Downloading of plugins just started for items: {}", getReport(pluginSet));

        return pluginDownloader.download(pluginSet.stream()
                .filter(Plugin::isDownloadable)
                .filter(plugin -> !plugin.getJarArchive().isStoredLocally())
                .collect(Collectors.toSet()))
                .map(pluginCompletableFuture -> pluginCompletableFuture.whenCompleteAsync(((plugin, throwable) -> {

//                    TODO logika jest, przetestować + zrefaktoryzować
                    Plugin pluginResolved = pluginResolver.resolve(infrastructureInit, plugin.getName(), plugin.getVersion());
                    String interfaceName = pluginResolved.getJarArchive().getExtensionManifest().getImplementsProperty();

                    ConfigDescription configDescription = ConfigDescription.builder()
                            .uuid(UUID.randomUUID())
                            .name(plugin.getName())
                            .version(plugin.getVersion())
                            .enabled(true)
                            .build();

                    boolean isAdded = pluginConfig.add(interfaceName, configDescription);
                    try {
                        pluginConfig.save();
                    } catch (PluginSystemCriticalException e) {
                        e.printStackTrace();
                    }

                    ActionRequest actionRequest = ActionRequest.builder()
                            .extensionType(ExtensionType.toEnum(interfaceName))
                            .pluginUuid(configDescription.getUuid())
                            .pluginName(plugin.getName())
                            .pluginVersion(plugin.getVersion())
                            .build();

                    ActionResponse actionResponse = this.startupExtension(actionRequest);

                    throwable.printStackTrace();
                }))).collect(Collectors.toSet());
    }
}
