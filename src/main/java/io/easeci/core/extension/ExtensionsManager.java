package io.easeci.core.extension;

import io.easeci.api.extension.ActionRequest;
import io.easeci.api.extension.ActionResponse;
import io.easeci.extension.ExtensionType;
import io.easeci.commons.YamlUtils;
import io.easeci.extension.Standalone;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelName.PLUGIN_EVENT;
import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelPrefix.*;
import static io.easeci.core.log.ApplicationLevelLogFacade.logit;
import static io.easeci.core.workspace.LocationUtils.getPluginsYmlLocation;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

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
        logit(PLUGIN_EVENT, "ExtensionManager instance creation process invoked", TWO);
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
        logit(PLUGIN_EVENT, "Declared plugins enabling started", TWO);
        Set<Plugin> resolvedPlugins = pluginResolver.resolve(pluginYml, infrastructureInit);
        Set<Plugin> pluginsNotResolved = pluginLoader.loadPlugins(resolvedPlugins, (PluginStrategy) pluginConfig);
        if (!pluginsNotResolved.isEmpty() && isDownloadProcessEnabled()) {
            downloadInFly(pluginsNotResolved);
        } else if (pluginsNotResolved.isEmpty()) {
            logit(PLUGIN_EVENT, "All plugins was loaded correctly.\nReport:\n" + getReport(resolvedPlugins), FOUR);
        }
    }

    @Override
    public PluginContainerState state() {
        return this.pluginContainer.state();
    }

    @Override
    public ActionResponse shutdownExtension(ActionRequest actionRequest) {
        if (isNull(actionRequest.getPluginUuid()) || isNull(actionRequest.getExtensionType())) {
            throw new IllegalStateException("Cannot process shutting down plugin because pluginUuid or extensionType is null");
        }
        logit(PLUGIN_EVENT, "Trying to finish plugin identified by UUID: " + actionRequest.getPluginUuid(), THREE);
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
            logit(PLUGIN_EVENT, "Stopping standalone plugin work " + instance.getPlugin().toShortString(), THREE);
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
            logit(PLUGIN_EVENT, "Stopping other than standalone plugin work " + instance.getPlugin().toShortString(), THREE);
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
        Optional<Instance> instanceOptional = this.pluginContainer.findByUuid(actionRequest.getExtensionType(), actionRequest.getPluginUuid());
        if (instanceOptional.isEmpty()) {
            logit(PLUGIN_EVENT, "Cannot find Instance by UUID=[{ " + actionRequest.getPluginUuid() + "}]", THREE);
            return ActionResponse.builder()
                    .isSuccessfullyDone(false)
                    .message("Cannot find any plugin with UUID=[" + actionRequest.getPluginUuid() + "] in container")
                    .build();
        }

//        Firstly, check is plugin running right now, and if yes return without further processing
        Optional<ActionResponse> actionResponse = instanceOptional.map(instance -> {
            if (instance.isStarted() && instance.isRunning()) {
                return ActionResponse.builder()
                        .isSuccessfullyDone(false)
                        .message("Plugin with UUID=[" + actionRequest.getPluginUuid() + "] is just correctly enabled, maybe try /restart ?")
                        .build();
            }
            return null;
        });
        if (actionResponse.isPresent()) {
            return actionResponse.get();
        }

        instanceOptional.ifPresent(instance -> {
                    Instance instanceReloaded = pluginLoader.reinstantiatePlugin(instance, (PluginStrategy) pluginConfig);
                    if (instanceReloaded.isStandalone()) {
                        List<Standalone> standaloneList = Collections.singletonList(instanceReloaded.toStandalone());
                        try {
                            PluginThreadPool.getInstance().run(standaloneList);
                        } catch (PluginSystemCriticalException e) {
                            e.printStackTrace();
                        }
                    } else {
                        int identityHashCode = System.identityHashCode(instanceReloaded.getInstance());
                        logit(PLUGIN_EVENT, "[Extension plugin] Correctly found Instance by hashCode[{"
                                + identityHashCode + "}], plugin: {" + instanceReloaded.getPlugin().toShortString() + "}", THREE);
                    }
                    pluginConfig.enable(actionRequest.getPluginUuid());
                });

        return ActionResponse.builder()
                .isSuccessfullyDone(true)
                .message("Plugin with UUID=[" + actionRequest.getPluginUuid() + "] is correctly enabled")
                .build();
    }

    @Override
    public ActionResponse restart(ActionRequest actionRequest) {
        logit(PLUGIN_EVENT, "[Extension plugin] Restarting of plugin: " + actionRequest.toString(), THREE);
        this.shutdownExtension(actionRequest);
        ActionResponse actionResponse = this.startupExtension(actionRequest);
        if (actionResponse.getIsSuccessfullyDone()) {
            actionResponse.setMessage("Plugin with UUID=[" + actionRequest.getPluginUuid() + "] is correctly restarted");
        }
        return actionResponse;
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

    private void downloadInFly(Set<Plugin> pluginSet) {
        logit(PLUGIN_EVENT, "Downloading of plugins just started for items:\n" + getReport(pluginSet), THREE);
        pluginSet.stream()
                .filter(Plugin::isDownloadable)
                .filter(plugin -> !plugin.getJarArchive().isStoredLocally())
                .collect(Collectors.toSet())
                .forEach(plugin -> pluginDownloader.download(plugin)
                        .thenApply(this::callResolver)
                        .whenComplete((this::loadOnFly)));
    }

    private Wrapper callResolver(Plugin pluginFuture) {
        Plugin pluginResolved = pluginResolver.resolve(infrastructureInit, pluginFuture.getName(), pluginFuture.getVersion());
        final Path JAR_PATH = pluginResolved.getJarArchive().getJarPath();
        try {
            final String INTERFACE_NAME = Utils.extractManifest(JAR_PATH).getImplementsProperty();
            final UUID PLUGIN_LOCAL_UUID = UUID.randomUUID();

            if (pluginConfig.add(INTERFACE_NAME, ConfigDescription.builder()
                                                                  .uuid(PLUGIN_LOCAL_UUID)
                                                                  .name(pluginFuture.getName())
                                                                  .version(pluginFuture.getVersion())
                                                                  .enabled(true)
                                                                  .build())) {
                try {
                    pluginConfig.save();
                } catch (PluginSystemCriticalException e) {
                    e.printStackTrace();
                }
            }

            return Wrapper.of(pluginResolved, ActionRequest.builder()
                    .extensionType(ExtensionType.toEnum(INTERFACE_NAME))
                    .pluginUuid(PLUGIN_LOCAL_UUID)
                    .pluginName(pluginResolved.getName())
                    .pluginVersion(pluginResolved.getVersion())
                    .build());
        } catch (IOException e) {
            e.printStackTrace();
            throw new PluginSystemRuntimeException("Error occurred while trying to read MANIFEST.MF file in jar file: " + JAR_PATH.toString());
        }
    }

    private void loadOnFly(Wrapper wrapper, Throwable throwable) {
        Set<Plugin> pluginsNotLoaded = pluginLoader.loadPlugins(Set.of(wrapper.plugin), (PluginStrategy) pluginConfig);
        if (!pluginsNotLoaded.isEmpty())
            logit(PLUGIN_EVENT, "Downloaded but not loaded: " + pluginsNotLoaded, THREE);

        ActionResponse actionResponse = this.startupExtension(wrapper.actionRequest);

        if (actionResponse.getIsSuccessfullyDone())
            logit(PLUGIN_EVENT, "Plugin " + wrapper.plugin.toShortString() + " correctly installed in EaseCI system", THREE);

        if (nonNull(throwable))
            throwable.printStackTrace();
    }

    @AllArgsConstructor(staticName = "of")
    private static class Wrapper {
        private final Plugin plugin;
        private final ActionRequest actionRequest;
    }
}
