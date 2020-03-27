package io.easeci.core.extension;

import java.util.*;

import static java.util.Objects.isNull;

class DefaultPluginContainer implements PluginContainer {
    private Map<String, List<Object>> container;

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
        return container.get(interfaceName);
    }
}
