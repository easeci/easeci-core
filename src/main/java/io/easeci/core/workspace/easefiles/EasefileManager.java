package io.easeci.core.workspace.easefiles;

import io.easeci.commons.DirUtils;
import io.easeci.core.workspace.easefiles.filetree.FileTree;

import java.nio.file.Path;
import java.util.List;

import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelName.WORKSPACE_EVENT;
import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelPrefix.THREE;
import static io.easeci.core.log.ApplicationLevelLogFacade.logit;
import static io.easeci.core.workspace.LocationUtils.getWorkspaceLocation;

public abstract class EasefileManager {
    public final static String EASEFILES_DIRECTORY = "/easefiles/",
                                EASEFILE_SEPARATOR = "_",
                                   EASEFILE_PREFIX = "Easefile";

    EasefileManager() {
        this.initializeDirectory();
    }

    private Path initializeDirectory() {
        final String workspaceLocation = getWorkspaceLocation();
        final String easefilesDirLocation = workspaceLocation.concat(EASEFILES_DIRECTORY);
        if (!DirUtils.isDirectoryExists(easefilesDirLocation)) {
            Path path = DirUtils.directoryCreate(easefilesDirLocation);
            logit(WORKSPACE_EVENT, "Directory for Easefiles just created at here: " + path, THREE);
            return path;
        }
        return Path.of(easefilesDirLocation);
    }

    /**
     * Use this method to scan and list all Easefiles recursively in whole workspace.
     * @return List<Path> that contains all directories and pipeline Easefiles stored in workspace
     * */
    abstract FileTree scan();
}
