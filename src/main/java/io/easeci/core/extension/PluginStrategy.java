package io.easeci.core.extension;

import java.util.List;

interface PluginStrategy {
    Instance choose(List<Instance> instanceList, String interfaceName);
}
