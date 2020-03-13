package io.easeci.core.extension;

import io.easeci.utils.io.DirUtils;
import io.easeci.utils.io.FileUtils;
import io.easeci.utils.io.YamlUtils;
import lombok.Getter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.easeci.core.workspace.LocationUtils.getPluginsYmlLocation;
import static io.easeci.core.workspace.LocationUtils.getWorkspaceLocation;

class ExtensionInfrastructureInit implements InfrastructureInit {
    private String current = System.getProperty("user.dir");
    private String workspace = getWorkspaceLocation();
    private Map<String, String> locations;

    @Getter
    private List<Path> pluginDirectories;

    ExtensionInfrastructureInit() {
        this.locations = new LinkedHashMap<>() {{
           put("<current>", current);
           put("<workspace>", workspace);
        }};

        this.pluginDirectories = ((List<String>) YamlUtils.ymlGet(getPluginsYmlLocation(), "plugins.localisations")
                .getValue())
                .stream()
                .map(pathFromProperty -> {
                    Pattern pattern = Pattern.compile("\\<[a-zA-Z0-9.\\-_]+\\>");
                    Matcher matcher = pattern.matcher(pathFromProperty);
                    String prepared = null;
                    while (matcher.find()) {
                        String group = matcher.group();
                        prepared = pathFromProperty.replace(group, locations.get(group));
                    }
                    return prepared;
                })
                .filter(Objects::nonNull)
                .map(stringPath -> Paths.get(stringPath))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isInitialized() {
        Path pluginYml = getPluginsYmlLocation();
        List<Path> allRequiredPaths = this.pluginDirectories;
        allRequiredPaths.add(pluginYml);

        for (Path path : pluginYml) {
            if (!Files.exists(path)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void prepareInfrastructure() {
        if (isInitialized()) {
            return;
        }
        this.pluginDirectories.forEach(path -> DirUtils.directoryCreate(path.toString()));
        createPluginYml(Paths.get(getWorkspaceLocation()));
    }

    private Path createPluginYml(Path targetPath) {
        return YamlUtils.ymlCreate(targetPath, new LinkedHashMap<>() {{
            put("plugins", new LinkedHashMap<>() {{
                put("localisations", List.of("<current>/plugins", "<workspace>/plugins"));
            }});
        }});
    }
}
