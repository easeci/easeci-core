package io.easeci.core.extension;

import io.easeci.utils.io.DirUtils;
import io.easeci.utils.io.FileUtils;
import io.easeci.utils.io.YamlUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.easeci.core.workspace.LocationUtils.getPluginsYmlLocation;
import static io.easeci.core.workspace.LocationUtils.getWorkspaceLocation;

@Slf4j
class ExtensionInfrastructureInit implements InfrastructureInit {
    private String current = System.getProperty("user.dir");
    private String workspace = getWorkspaceLocation();
    private Map<String, String> locations = new LinkedHashMap<>() {{
        put("<current>", current);
        put("<workspace>", workspace);
    }};

    @Getter
    private List<Path> pluginDirectories = new ArrayList<>(1);

    @Override
    public void loadInfrastructure() throws Exception {
        if (!FileUtils.isExist(getPluginsYmlLocation().toString())) {
            throw new Exception("Cannot load infrastructure because plugins file not exists. " +
                    "It seems to look like prepareInfrastructure() method was not invoked. Please do this first of.");
        }
        this.pluginDirectories = ((List<String>) YamlUtils.ymlGet(getPluginsYmlLocation(), "plugins.local.localisations")
                .getValue())
                .stream()
                .map(pathFromProperty -> {
                    Pattern pattern = Pattern.compile("\\<[a-zA-Z0-9.\\-_]+\\>");
                    Matcher matcher = pattern.matcher(pathFromProperty);
                    while (matcher.find()) {
                        String group = matcher.group();
                        pathFromProperty = pathFromProperty.replace(group, Optional.ofNullable(locations.get(group)).orElse(group));
                    }
                    return pathFromProperty;
                })
                .map(stringPath -> Paths.get(stringPath))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isInitialized() {
        Path pluginYml = getPluginsYmlLocation();
        if (!FileUtils.isExist(pluginYml.toString())) {
            return false;
        }

        List<Path> allRequiredPaths = this.pluginDirectories;
        if (allRequiredPaths.isEmpty()) {
            return false;
        }
        allRequiredPaths.add(pluginYml);

        for (Path path : allRequiredPaths) {
            if (!Files.exists(path)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void prepareInfrastructure() throws Exception {
        if (isInitialized()) {
            log.info("===> Infrastructure for plugin management is already correctly initialized");
            return;
        }
        log.info("===> Infrastructure for plugin management not exist or is malformed, started to prepare infrastructure");
        Path pluginsYmlLocation = getPluginsYmlLocation();
        if (!FileUtils.isExist(pluginsYmlLocation.toString())) {
            createMinimalisticPluginYml(getPluginsYmlLocation(), List.of("<current>/plugins", "<workspace>/plugins"));
        }
        this.loadInfrastructure();
        this.pluginDirectories.forEach(path -> {
            if (!DirUtils.isDirectoryExists(path.toString())) {
                DirUtils.directoryCreate(path.toString());
            }
        });
        this.prepareInfrastructure();
    }

    Path createMinimalisticPluginYml(Path targetPath, List<String> paths) {
        return YamlUtils.ymlCreate(targetPath, new LinkedHashMap<>() {{
            put("plugins", new LinkedHashMap<>() {{
                put("local", new LinkedHashMap<>() {{
                    put("localisations", paths);
                }});
            }});
        }});
    }
}
