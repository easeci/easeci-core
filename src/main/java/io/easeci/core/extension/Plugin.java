package io.easeci.core.extension;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.net.URL;
import java.nio.file.Path;

import static java.util.Objects.isNull;

@Getter
@AllArgsConstructor
public class Plugin {
    private String name;
    private String version;
    private JarArchive jarArchive;

    public static Plugin of(String name, String version) {
        if (isNull(name)) {
            throw new RuntimeException("Cannot create Plugin.class instance, name of plugin is null");
        }
        if (isNull(version)) {
            throw new RuntimeException("Cannot create Plugin.class instance, version of plugin is null");
        }
        return new Plugin(name, version, JarArchive.empty());
    }

    @Override
    public String toString() {
        return "~ Plugin '".concat(name)
                .concat("' ver. ")
                .concat(version);
    }

    @Getter
    @AllArgsConstructor
    public static class JarArchive {
        private URL jarUrl;
        private Path jarPath;

        private JarArchive() {}

        public static JarArchive empty() {
            return new JarArchive();
        }
    }
}
