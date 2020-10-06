package io.easeci.core.workspace.easefiles;

import io.easeci.core.workspace.easefiles.filetree.FileTree;
import io.easeci.core.workspace.easefiles.filetree.FileTreeWalker;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelName.WORKSPACE_EVENT;
import static io.easeci.core.log.ApplicationLevelLogFacade.logit;
import static io.easeci.core.workspace.LocationUtils.getEasefilesStorageLocation;
import static java.util.Objects.isNull;

public class DefaultEasefileManager extends EasefileManager {
    private static DefaultEasefileManager easefileManager;

    private DefaultEasefileManager() {}

    public static DefaultEasefileManager getInstance() {
        if (isNull(DefaultEasefileManager.easefileManager)) {
            DefaultEasefileManager.easefileManager = new DefaultEasefileManager();
        }
        return DefaultEasefileManager.easefileManager;
    }

    @Override
    FileTree scan() {
        Path easefilesStorageLocation = Paths.get(getEasefilesStorageLocation());
        FileTreeWalker fileTreeWalker = new FileTreeWalker(easefilesStorageLocation);
        try {
            return fileTreeWalker.dumpAll();
        } catch (IOException e) {
            e.printStackTrace();
            logit(WORKSPACE_EVENT, "Exception occurred while trying to scan and walkthrough directory: " + easefilesStorageLocation.toString());
            return FileTree.empty(easefilesStorageLocation);
        }
    }

}
