package io.easeci.core.extension;

import io.easeci.utils.io.FileUtils;
import io.easeci.utils.io.YamlUtils;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
class DefaultPluginConfig implements PluginConfig, PluginStrategy {
    private Path pluginConfigYmlPath;
    private PluginsConfigFile pluginsConfigFile;

    DefaultPluginConfig(Path pluginConfigYmlPath) {
        this.pluginConfigYmlPath = pluginConfigYmlPath;
        if (isNull(this.pluginConfigYmlPath) || !FileUtils.isExist(this.pluginConfigYmlPath.toString())) {
            throw new IllegalStateException("PluginsConfigFile is null or file not exists!");
        }
        this.pluginsConfigFile = this.load();
    }

    @Override
    public PluginsConfigFile load() {
        Map<?, ?> values = YamlUtils.ymlLoad(this.pluginConfigYmlPath);
        try {
            this.pluginsConfigFile = PluginsConfigFile.of(values);
            return this.pluginsConfigFile;
        } catch (ClassCastException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    @Override
    public synchronized PluginsConfigFile save(PluginsConfigFile pluginsConfigFile) {
//        TODO implement!
        return null;
    }

    @Override
    public boolean enable(UUID pluginUuid) {
        return false;
    }

    @Override
    public boolean disableAll(String interfaceName) {
        return false;
    }

    @Override
    public Instance choose(List<Instance> instanceList, String interfaceName) {
        if (isNull(instanceList) || isNull(interfaceName)) {
            return null;
        } if (instanceList.size() == 1) {
            return instanceList.get(0);
        } if (instanceList.size() > 1) {
            return instanceList.stream()
                    .filter(this.pluginsConfigFile.getConfigDescriptions()
                            .get(interfaceName)
                            .stream()
                            .filter(ConfigDescription::getEnabled)
                            .findFirst()
                            .orElse(ConfigDescription.empty()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}

@Slf4j
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class PluginsConfigFile {
    private Map<String, List<ConfigDescription>> configDescriptions;

    @SuppressWarnings("unchecked")
    static PluginsConfigFile of(Map<?, ?> ymlValues) throws ClassCastException, YamlUtils.YamlException {
        Map<String, List<ConfigDescription>> configDescriptions = new HashMap<>();
        List<Map<?, ?>> items = (List<Map<?, ?>>) YamlUtils.ymlGet(ymlValues, "extension").getValue();
        if (nonNull(items)) {
            items.forEach(map -> {
                List<Map<String, ?>> plugins = (List<Map<String, ?>>) map.get("item");
                String interfaceName = (String) map.get("interface");
                plugins.forEach(plugin -> add(configDescriptions, interfaceName, ConfigDescription.of((Map<?, ?>) plugin.get("plugin"))));
            });
            return new PluginsConfigFile(configDescriptions);
        }
        return new PluginsConfigFile();
    }

    private static void add(Map<String, List<ConfigDescription>> configDescriptionsMap, String interfaceName, ConfigDescription configDescription) {
        List<ConfigDescription> configDescriptionList = configDescriptionsMap.get(interfaceName);
        if (isNull(configDescriptionList)) {
            configDescriptionsMap.put(interfaceName, new ArrayList<>(Collections.singletonList(configDescription)));
        } else {
            if (configDescriptionList.contains(configDescription)) {
                log.error("===> Cannot add two the same PluginConfigFile data objects for this one: {}", configDescription);
                return;
            }
            configDescriptionList.add(configDescription);
        }
    }
}

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class ConfigDescription implements Predicate<Instance> {
    private UUID uuid;
    private String name;
    private String version;
    private Boolean enabled;

    static ConfigDescription of(Map<?, ?> values) throws ClassCastException {
        UUID uuid = UUID.fromString((String) values.get("uuid"));
        String name = (String) values.get("name");
        String version = (String) values.get("version");
        Boolean enabled = (Boolean) values.get("enabled");
        return new ConfigDescription(uuid, name, version, enabled);
    }

    static ConfigDescription empty() {
        return new ConfigDescription(UUID.randomUUID(), "", "", false);
    }

    @Override
    public boolean test(Instance instance) {
        return this.name.equals(instance.getPlugin().getName())
                && this.version.equals(instance.getPlugin().getVersion())
                && this.enabled;
    }
}
