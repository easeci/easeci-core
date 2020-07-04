package io.easeci.core.extension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.easeci.commons.FileUtils;
import io.easeci.extension.ExtensionType;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

import static java.util.Objects.isNull;

@Slf4j
class DefaultPluginConfig implements PluginConfig, PluginStrategy {
    private final static ObjectMapper JSON_MAPPER = new ObjectMapper();
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
        try {
            this.pluginsConfigFile = JSON_MAPPER.readValue(this.pluginConfigYmlPath.toFile(), PluginsConfigFile.class);
            return this.pluginsConfigFile;
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    @Override
    public synchronized PluginsConfigFile save() {
        try {
            String content = JSON_MAPPER.writeValueAsString(this.pluginsConfigFile);
            Path path = FileUtils.fileChange(this.pluginConfigYmlPath.toString(), content);
            log.info("===> PluginsConfigFile saved in: {}", path.toString());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return this.pluginsConfigFile;
        }
        return load();
    }

    @Override
    public boolean add(String interfaceName, ConfigDescription configDescription) {
        return this.pluginsConfigFile.put(interfaceName, configDescription);
    }

    @Override
    public boolean update(ConfigDescription configDescription) {
        return false;
    }

    @Override
    public boolean remove(ConfigDescription configDescription) {
        return false;
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

    @Override
    public PluginsConfigFile pluginsConfigFile() {
        return this.pluginsConfigFile;
    }

    @Override
    public ConfigDescription find(ExtensionType extensionType, String pluginName, String pluginVersion) {
        return this.pluginsConfigFile.getConfigDescriptions().get(ExtensionType.toInterface(extensionType))
                .stream()
                .filter(configDescription -> configDescription.getName().equals(pluginName) && configDescription.getVersion().equals(pluginVersion))
                .findAny()
                .orElseThrow(PluginSystemIntegrityViolated::new);
    }

    @Override
    public ConfigDescription find(ExtensionType extensionType, UUID uuid) {
        return this.pluginsConfigFile.getConfigDescriptions().get(ExtensionType.toInterface(extensionType))
                .stream()
                .filter(configDescription -> configDescription.getUuid().equals(uuid))
                .findAny()
                .orElseThrow(PluginSystemIntegrityViolated::new);
    }
}

@Slf4j
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class PluginsConfigFile {
    private Map<String, Set<ConfigDescription>> configDescriptions;

    boolean put(String interfaceName, ConfigDescription configDescription) {
        Set<ConfigDescription> configDescriptionSet = this.configDescriptions.get(interfaceName);
        if (isNull(configDescriptionSet)) {
            this.configDescriptions.put(interfaceName, Set.of(configDescription));
        } else {
            configDescriptionSet.add(configDescription);
        }
        return true;
    }
}

@Getter
@Builder
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class ConfigDescription implements Predicate<Instance> {
    private UUID uuid;
    private String name;
    private String version;
    private Boolean enabled;

    static ConfigDescription empty() {
        return new ConfigDescription(UUID.randomUUID(), "", "", false);
    }

    @Override
    public boolean test(Instance instance) {
        return this.name.equals(instance.getPlugin().getName())
                && this.version.equals(instance.getPlugin().getVersion())
                && this.enabled;
    }

    @Override
    public boolean equals(Object obj) {
        ConfigDescription configDescription = (ConfigDescription) obj;
        return this.name.equals(configDescription.name)
                && this.version.equals(configDescription.version);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
