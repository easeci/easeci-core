package io.easeci.core.workspace;

import io.easeci.core.workspace.easefiles.EasefileManager;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.easeci.core.workspace.AbstractWorkspaceInitializer.BOOTSTRAP_FILENAME;
import static io.easeci.commons.YamlUtils.ymlGet;
import static io.easeci.core.workspace.projects.ProjectManager.PIPELINES_DIRECTORY;

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

    public static String getEasefilesStorageLocation() {
        return ((String) ymlGet(getRunFile().toPath(), "easeci.workspace.path").getValue())
                .concat(EasefileManager.EASEFILES_DIRECTORY);
    }

    public static String getEasefilesStorageLocationNoSlashAtEnd() {
        String location = getEasefilesStorageLocation();
        if (location.charAt(location.length() - 1) == '/') {
            return location.substring(0, location.length() - 1);
        }
        return location;
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

    public static Path getPluginsYmlLocation() {
        return Paths.get(getWorkspaceLocation()
                .concat("/plugins.yml"));
    }

    public static Path getPluginConfigYmlLocation() {
        return Paths.get(getWorkspaceLocation()
                .concat("/plugins-config.json"));
    }

    public static Path getCacheDirectoryLocation() {
        return Paths.get(getWorkspaceLocation()
                    .concat("/.cache"));
    }
  
    public static Path getProjectsDirectoryLocation() {
        return Paths.get(getWorkspaceLocation()
                    .concat("/projects"));
    }

    public static Path getProjectsStructureFileLocation() {
        return Paths.get(getWorkspaceLocation()
                    .concat("/projects/projects-structure.json"));
    }

    public static Path getVarsFileLocation() {
        return Paths.get(getWorkspaceLocation()
                    .concat("/vars.json"));
    }

    public static Path getPipelineFilesLocation() {
        return Paths.get(getWorkspaceLocation()
                    .concat(PIPELINES_DIRECTORY));
    }

    public static Path getPipelineRunLogLocation() {
        return Paths.get(getWorkspaceLocation()
                    .concat("/log/context"));
    }

    public static Path getPipelineRunHistoryLogLocation() {
        return Paths.get(getWorkspaceLocation()
                    .concat("/log/history"));
    }

    public static Path getPipelineRunHistoryLogFileLocation() {
        return Paths.get(getWorkspaceLocation()
                    .concat("/log/history/pipeline-run-history"));
    }
}
