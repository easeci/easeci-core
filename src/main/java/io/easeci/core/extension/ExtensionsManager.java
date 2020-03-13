package io.easeci.core.extension;

import io.easeci.utils.io.YamlUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.easeci.core.workspace.LocationUtils.getPluginsYmlLocation;
import static io.easeci.core.workspace.LocationUtils.getWorkspaceLocation;
import static java.util.Objects.isNull;

@Slf4j
public class ExtensionsManager {
    private static ExtensionsManager extensionsManager;

    @Getter
    private Set<Plugin> pluginSet;

    private ExtensionsManager() {}

    public static ExtensionsManager getInstance() {
        if (isNull(extensionsManager)) {
            extensionsManager = new ExtensionsManager();
        }
        return extensionsManager;
    }

    public Set<Plugin> parsePluginFile() {
        log.info("====> Plugin file parse started");
        Path pluginsYmlLocation = getPluginsYmlLocation();

        ExtensionInfrastructureInit extensionInfrastructureInit = new ExtensionInfrastructureInit();

//        List<LinkedHashMap> pluginList = (List<LinkedHashMap>) YamlUtils.ymlGet(pluginsYmlLocation, "plugins").getValue();
        List<LinkedHashMap> pluginList = List.of();

        return pluginList.stream()
                .map(hashMap -> Plugin.of((String) hashMap.get("name"), (String) hashMap.get("version")))
                .peek(plugin -> log.info("=====> {}", plugin.toString()))
                .collect(Collectors.toSet());
    }

//    public Set<> searchForPlugin() {
//        final String PLUGIN_DIR = "plugins";
//        List<Path> localizations = List.of(
//                Paths.get(System.getProperty("user.dir").concat(PLUGIN_DIR)),
//                Paths.get(getWorkspaceLocation().concat(PLUGIN_DIR))
//        );
//
//
//    }

    /*
    * Algorytm ładowania pluginu:
    * - tworzymy katalog do trzymania pluginów
    * - na starcie szukamy pluginów:
    *    a) katalog plugin/ równoległy do tego gdzie znajduje się .jar
    *    b) katalog plugin/ w workspace/
    *    c) Jeśli nie ma ani tu ani tu, to pobieramy z easeci-registry i zapisujemy tam gdzie wskazane jest w yamlu,
    *       z defaultu w katalogu równoległego do naszego głównego .jar
    * - ładujemy klasy jara do bieżącego procesu aplikacji
    *
    * ! skąd będę wiedział, że dodałem nową implementację?
    * */
}
