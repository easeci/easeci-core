package io.easeci.core.extension;

import io.easeci.utils.io.YamlUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.easeci.core.workspace.LocationUtils.getPluginsYmlLocation;
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

    public void parsePluginFile() {
        log.info("====> Plugin file parse started");
        Path pluginsYmlLocation = getPluginsYmlLocation();

        List<LinkedHashMap> pluginList = (List<LinkedHashMap>) YamlUtils.ymlGet(pluginsYmlLocation, "plugins").getValue();

        this.pluginSet = pluginList.stream()
                .map(hashMap -> Plugin.of((String) hashMap.get("name"), (String) hashMap.get("version")))
                .peek(plugin -> log.info("=====> {}", plugin.toString()))
                .collect(Collectors.toSet());
    }

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
