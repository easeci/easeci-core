package io.easeci.core.extension;

interface PluginContainer {

    void add(String interfaceName, Object implementation);

    Object get(String interfaceName);
}
