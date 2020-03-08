package io.easeci.core.workspace;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.easeci.core.workspace.AbstractWorkspaceInitializer.BOOTSTRAP_FILENAME;
import static io.easeci.utils.io.YamlUtils.ymlGet;

public class LocationUtils {

    public static File getRunFile() {
        return Path.of(System.getProperty("user.dir")
                .concat("/")
                .concat(BOOTSTRAP_FILENAME))
                .toFile();
    }

    public static String getWorkspaceLocation() {
        return (String) ymlGet(getRunFile().toPath(), "easeci.workspace.path").getValue();
    }

    public static Path getGeneralYmlLocation() {
        return Paths.get(getWorkspaceLocation()
                .concat("/general.yml"));
    }

    public static String retrieveFromGeneral(String refs) throws Throwable {
        String result = (String) ymlGet(getGeneralYmlLocation(), refs).getValue();
        if (result == null) {
            throw new Exception("Cannot find value in yaml [" + refs + "]");
        }
        return result;
    }

    public static Integer retrieveFromGeneralInt(String refs) throws Throwable {
        Integer result = (Integer) ymlGet(getGeneralYmlLocation(), refs).getValue();
        if (result == null) {
            throw new Exception("Cannot find value in yaml [" + refs + "]");
        }
        return result;
    }
}
