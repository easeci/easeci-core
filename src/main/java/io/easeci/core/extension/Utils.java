package io.easeci.core.extension;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utils space for io.easeci.core.extension package.
 * */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class Utils {
    private final static String SEPARATOR = "-",
                                SLASH = "/",
                                EXTENSION = ".jar";

    /**
     * Build file name of plugin's jar in trust way
     * @param name is name of plugin
     * @param version is version of plugin
     * @return string representation of plugin's file name
     * */
    static String pluginFileName(String name, String version) {
        return name.concat(SEPARATOR).concat(version).concat(EXTENSION);
    }

    /**
     * Get likely locations where plugin file is placed
     * on local storage.
     * @param pluginDirectories is path to directories where
     *                          plugin could be stored (most likely data from yaml files)
     * @param name is name of plugin
     * @param version is version of plugin
     * */
    static Set<Path> likelyLocations(List<Path> pluginDirectories, String name, String version) {
        return pluginDirectories.stream()
                .map(Path::toString)
                .map(pathAsString -> pathAsString.concat(SLASH).concat(pluginFileName(name, version)))
                .map(pathAsString -> Paths.get(pathAsString))
                .collect(Collectors.toSet());
    }

    /**
     * Prepare full URL for download plugin from EaseCI registry server.
     * @param registryUrl is address of registry server: http://localhost:8080 or https://easeci-registry.io
     * @param pluginName is name of plugin
     * @param pluginVersion is version of plugin
     * @return complete GET URL, example:
     *      http://localhost:8080/api/v1/download/time-printer/0.0.1
     * */
    static String completePluginDownloadUrl(URL registryUrl, String pluginName, String pluginVersion) {
        final String REST_API_ENDPOINT = "/api/v1/download/";
        return registryUrl.toString()
                .concat(REST_API_ENDPOINT)
                .concat(pluginName)
                .concat(SLASH)
                .concat(pluginVersion);
    }
}
