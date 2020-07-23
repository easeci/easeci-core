package io.easeci.core.extension;

import io.easeci.commons.FileUtils;
import io.easeci.commons.YamlUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.easeci.core.extension.Utils.likelyLocations;
import static io.easeci.core.extension.Utils.pluginFileName;
import static io.easeci.commons.YamlUtils.ymlGet;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
class DefaultPluginResolver implements PluginResolver {
    private final static String NAME = "name",
                             VERSION = "version";

    @Override
    public Set<Plugin> resolve(Path pluginYml, PluginInfrastructureInfo infrastructureInfo) {
        if (isNull(pluginYml) || !FileUtils.isExist(pluginYml.toString())
                || isNull(infrastructureInfo) || isNull(infrastructureInfo.getPluginDirectories())) {
            log.error("=> Cannot execute resolve() method because of method's arguments are not correct");
            return Collections.emptySet();
        }
        return PluginsFile.create(pluginYml).getPluginsList()
                .stream()
                .flatMap(nestedMap -> nestedMap.values().stream())
                .filter(pluginAsMap -> nonNull(pluginAsMap.get(NAME)) && nonNull(pluginAsMap.get(VERSION)))
                .map(pluginAsMap -> Plugin.of(pluginAsMap.get(NAME), pluginAsMap.get(VERSION)))
                .map(plugin -> Plugin.of(plugin, prepareJar(infrastructureInfo.getPluginDirectories(), plugin.getName(), plugin.getVersion())))
                .collect(Collectors.toSet());
    }

    @Override
    public Plugin resolve(PluginInfrastructureInfo infrastructureInfo, String pluginName, String pluginVersion) {
        Plugin plugin = Plugin.of(pluginName, pluginVersion);
        Plugin.JarArchive jarArchive = prepareJar(infrastructureInfo.getPluginDirectories(), pluginName, pluginVersion);
        return Plugin.of(plugin, jarArchive);
    }

    private Plugin.JarArchive prepareJar(List<Path> pluginDirectories, String name, String version) {
        String pluginFileName = pluginFileName(name, version);
        Path jarFilePath = likelyLocations(pluginDirectories, name, version)
                .stream()
                .filter(path -> Files.exists(path))
                .findFirst()
                .orElse(null);
        URL jarFileUrl = toUrl(jarFilePath);
        boolean isExists = isJarExists(jarFilePath);

        return Plugin.JarArchive.of(pluginFileName, isExists, jarFileUrl, jarFilePath, null);
    }

    private URL toUrl(Path jarFilePath) {
        if (isNull(jarFilePath)) {
            return null;
        }
        try {
            return jarFilePath.toUri().toURL();
        } catch (MalformedURLException e) {
            log.error("Exception occurred while transforming path to URL object " + e.getCause());
            return null;
        }
    }

    private boolean isJarExists(Path jarFilePath) {
        if (isNull(jarFilePath)) {
            return false;
        }
        return Files.exists(jarFilePath);
    }
}

/**
 * PluginFile is a POJO representation of required information
 * included in plugins.yml file.
 * */
@Getter
@ToString
@AllArgsConstructor
class PluginsFile {
    private long registryTimeout;
    private String registryUrl;
    private List<Map<String, Map<String, String>>> pluginsList;

    static PluginsFile create(Path pluginYml) {
        Map<?, ?> map = YamlUtils.ymlLoad(pluginYml);
        Integer registryTimeout = (Integer) YamlUtils.ymlGet(map, "plugins.registry.timeout").getValue();
        String registryUrl = (String) YamlUtils.ymlGet(map, "plugins.registry.url").getValue();
        List<Map<String, Map<String, String>>> pluginsList = (List<Map<String, Map<String, String>>>) ymlGet(map, "extensions").getValue();
        return new PluginsFile(registryTimeout, registryUrl, pluginsList);
    }
}
