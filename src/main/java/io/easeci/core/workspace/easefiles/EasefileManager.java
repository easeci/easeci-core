package io.easeci.core.workspace.easefiles;

import io.easeci.commons.DirUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelName.WORKSPACE_EVENT;
import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelPrefix.THREE;
import static io.easeci.core.log.ApplicationLevelLogFacade.logit;
import static io.easeci.core.workspace.LocationUtils.getWorkspaceLocation;

/**
 * Instance that manage all Easefile in system.
 * Only instance of this object should be able to
 * manage pipelines file in application.
 * @author Karol Meksuła
 * 2020-10-06
 * */
public abstract class EasefileManager implements FileScanner, EasefileIO {
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

    public Path getRootEasefilePath() {
        final String workspaceLocation = getWorkspaceLocation();
        return Paths.get(workspaceLocation.concat(EASEFILES_DIRECTORY));
    }
}
