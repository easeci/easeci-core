package io.easeci.core.extension;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.net.URL;
import java.nio.file.Path;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

/**
 * Representation of the plugin as POJO.
 * About naming (String name field):
 *   - use only alphanumeric characters in plugin's naming
 *   - the word separator used is a dash: '-'
 * About versioning (String version field):
 *   - versioning must to be compatible with semantic versioning
 *     ex. '1.11.0'
 * */

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

    public static Plugin of(Plugin plugin, JarArchive jarArchive) {
        if (isNull(plugin.name)) {
            throw new RuntimeException("Cannot create Plugin.class instance, name of plugin is null");
        }
        if (isNull(plugin.version)) {
            throw new RuntimeException("Cannot create Plugin.class instance, version of plugin is null");
        }
        return new Plugin(plugin.name, plugin.version, jarArchive);
    }

    boolean isLoadable() {
        return nonNull(this.name)
                && nonNull(this.version)
                && nonNull(this.jarArchive)
                && nonNull(this.jarArchive.fileName)
                && nonNull(this.jarArchive.jarPath)
                && nonNull(this.jarArchive.jarUrl);
    }

    boolean isDownloadable() {
        return nonNull(this.name)
                && !this.name.isEmpty()
                && nonNull(this.version)
                && !this.version.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        Plugin next = (Plugin) obj;
        return this.name.equals(next.name) && this.version.equals(next.version);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() + this.version.hashCode();
    }

    @Override
    public String toString() {
        return "~ Plugin '".concat(name)
                .concat("' ver. ")
                .concat(version)
                .concat(", localised ")
                .concat((isNull(this.jarArchive) ? "" : this.jarArchive.toString()));
    }

    public String toShortString() {
        return "~ Plugin '".concat(name)
                .concat("' ver. ")
                .concat(version);
    }

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class JarArchive {
        private final static String DASH = "-";
        private String fileName;
        private boolean isStoredLocally;
        private URL jarUrl;
        private Path jarPath;

        @Setter
        private ExtensionManifest extensionManifest;

        private JarArchive() {}

        public static JarArchive empty() {
            return new JarArchive();
        }

        @Override
        public String toString() {
            return "~ Jar file named: ".concat(ofNullable(fileName).orElse(DASH))
                    .concat(", localised here: ")
                    .concat(ofNullable(jarPath).orElse(Path.of(DASH)).toString())
                    .concat(", is exists: " + isStoredLocally);
        }
    }
}
