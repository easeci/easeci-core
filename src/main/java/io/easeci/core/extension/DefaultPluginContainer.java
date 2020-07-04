package io.easeci.core.extension;

import io.easeci.extension.ExtensionType;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

@Slf4j
class DefaultPluginContainer implements PluginContainer {
    private Map<String, List<Instance>> container;
    private PluginStrategy pluginStrategy;

    DefaultPluginContainer(PluginStrategy pluginStrategy) {
        this.container = new ConcurrentHashMap<>();
        this.pluginStrategy = pluginStrategy;
    }

    @Override
    public void add(Instance instance) {
        final String interfaceName = instance.getPlugin().getJarArchive().getExtensionManifest().getImplementsProperty();
        List<Instance> objectList = this.container.get(interfaceName);
        if (isNull(objectList)) {
            this.container.put(interfaceName, new ArrayList<>(Collections.singletonList(instance)));
        } else {
            if (objectList.contains(instance)) {
                log.error("===> Cannot add two the same plugin implementations for this one: {}", instance.getPlugin());
                return;
            }
            objectList.add(instance);
        }
    }

    @Override
    public <T> T getSpecific(String interfaceName, Class<T> type) {
        Instance instance = get(interfaceName);
        return cast(instance, type);
    }

    @Override
    public Optional<Instance> findByUuid(ExtensionType extensionType, UUID pluginUuid) {
        ConfigDescription configDescription = this.pluginStrategy.find(extensionType, pluginUuid);

        return this.container.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(instance -> instance.getPlugin().getName().equals(configDescription.getName()) &&
                                    instance.getPlugin().getVersion().equals(configDescription.getVersion()))
                .findAny();
    }

    @Override
    public <T> List<T> getGathered(String interfaceName, Class<T> type) {
        List<Instance> instances = container.get(interfaceName);
        if (isNull(instances) || instances.isEmpty()) {
            return Collections.emptyList();
        }
        return instances
                .stream()
                .dropWhile(instance -> isNull(instance.getInstance()))
                .map(instance -> cast(instance, type))
                .dropWhile(Objects::isNull)
                .collect(Collectors.toList());
    }

    private Instance get(String interfaceName) {
        List<Instance> instanceList = ofNullable(container.get(interfaceName)).orElse(Collections.emptyList());
        return pluginStrategy.choose(instanceList, interfaceName);
    }

    private <T> T cast(Instance instance, Class<T> type) {
        if (isNull(instance)) {
            return null;
        }
        try {
            return type.cast(Objects.requireNonNull(instance).getInstance());
        } catch (ClassCastException | NullPointerException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    @Override
    public <T> T remove(String interfaceName, Predicate<Object> toRemove, Class<T> implementationType) {
//        TODO implement!
        return null;
    }

    @Override
    public PluginContainerState state(ExtensionType extensionType) {
        return new PluginContainerState(mapContainer());
    }

    private List<PluginState> mapContainer() {
        return container.entrySet()
                .stream()
                .flatMap(entry -> entry.getValue()
                        .stream()
                        .map(instance -> PluginStateProxy.of(entry.getKey(), instance)))
                .map(pluginStateProxy -> pluginStateProxy.toPluginState(pluginStrategy))
                .collect(Collectors.toList());
    }

    @Override
    public int size() {
        return this.container.size();
    }

    @Override
    public int implementationSize(String interfaceName) {
        return this.container.get(interfaceName).size();
    }
}
