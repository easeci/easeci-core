package io.easeci.core.extension;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static java.util.Objects.isNull;

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
    public Object get(String interfaceName) {
//        TODO
        log.error("Method requires implementation with priority choosing");
        return container.get(interfaceName).get(0);
    }
}
