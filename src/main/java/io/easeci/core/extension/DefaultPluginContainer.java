package io.easeci.core.extension;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

@Slf4j
class DefaultPluginContainer implements PluginContainer {
    private Map<String, List<Instance>> container;

    DefaultPluginContainer() {
        this.container = new ConcurrentHashMap<>();
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
        Object instance = get(interfaceName);
        try {
            return type.cast(instance);
        } catch (ClassCastException exception) {
            return null;
        }
    }

    @Override
    public <T> T remove(String interfaceName, Predicate<Object> toRemove, Class<T> implementationType) {
//        TODO implement!
        return null;
    }

    @Override
    public String state() {
//        TODO implement!
        return null;
    }

    @Override
    public int size() {
        return this.container.size();
    }

    @Override
    public int implementationSize(String interfaceName) {
        return this.container.get(interfaceName).size();
    }

    private Object get(String interfaceName) {
        log.error("Method requires implementation with priority choosing");
        List<Instance> instanceList = ofNullable(container.get(interfaceName)).orElse(Collections.emptyList());
        if (!instanceList.isEmpty()) {
            return instanceList.get(0).getInstance();         //        TODO implement!
        }
        return null;
    }
}
