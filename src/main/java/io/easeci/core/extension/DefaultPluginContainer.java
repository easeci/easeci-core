package io.easeci.core.extension;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Predicate;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

@Slf4j
class DefaultPluginContainer implements PluginContainer {
    private Map<String, List<Object>> container;

    DefaultPluginContainer() {
        this.container = new LinkedHashMap<>();
    }

    @Override
    public void add(String interfaceName, Object implementation) {
        List<Object> objectList = this.container.get(interfaceName);
        if (isNull(objectList)) {
            this.container.put(interfaceName, new ArrayList<>(Collections.singletonList(implementation)));
        } else {
            objectList.add(implementation);
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

    private Object get(String interfaceName) {
        log.error("Method requires implementation with priority choosing");
        List<Object> objectList = ofNullable(container.get(interfaceName)).orElse(Collections.emptyList());
        if (!objectList.isEmpty()) {
            return objectList.get(0);         //        TODO implement!
        }
        return null;
    }
}
