package io.easeci.core.extension;

import io.easeci.utils.io.YamlUtils;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.*;

import static java.util.Objects.isNull;

class DefaultPluginConfig implements PluginConfig {
    private Path pluginConfigYmlPath;
    private PluginsConfigFile pluginsConfigFile;

    DefaultPluginConfig(Path pluginConfigYmlPath) {
        this.pluginConfigYmlPath = pluginConfigYmlPath;
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
    static PluginsConfigFile of(Map<?, ?> ymlValues) throws ClassCastException {
        Map<String, List<ConfigDescription>> configDescriptions = new HashMap<>();
        List<Map<?, ?>> items = (List<Map<?, ?>>) YamlUtils.ymlGet(ymlValues, "extension").getValue();
        items.forEach(map -> {
            List<Map<String, ?>> plugins = (List<Map<String, ?>>) map.get("item");
            String interfaceName = (String) map.get("interface");
            plugins.forEach(plugin -> {
                add(configDescriptions, interfaceName, ConfigDescription.of((Map<?, ?>) plugin.get("plugin")));
            });
        });
        return new PluginsConfigFile(configDescriptions);
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

@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class ConfigDescription {
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
}
