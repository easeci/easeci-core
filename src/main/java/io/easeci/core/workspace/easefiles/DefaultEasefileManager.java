package io.easeci.core.workspace.easefiles;

import io.easeci.commons.FileUtils;
import io.easeci.core.log.ApplicationLevelLogFacade;
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
    public FileTree scan() {
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

    @Override
    public FileTree scan(Path path) {
        String easefilesStorageLocation = getEasefilesStorageLocation();
        if (!path.toString().startsWith(easefilesStorageLocation)) {
            logit(WORKSPACE_EVENT, "Forbidden to scan file tree for path "
                                 + path.toString() + ". Enable scan paths starts with: "
                                 + easefilesStorageLocation);
            return FileTree.empty(path);
        } else {
            FileTreeWalker fileTreeWalker = new FileTreeWalker(Paths.get(easefilesStorageLocation));
            try {
                return fileTreeWalker.dumpOne();
            } catch (IOException e) {
                e.printStackTrace();
                logit(WORKSPACE_EVENT, "Exception occurred while trying to scan and walkthrough directory: " + easefilesStorageLocation);
                return FileTree.empty(path);
            }
        }
    }

    @Override
    public String load(Path path) {
        return FileUtils.fileLoad(path.toString());
    }

    @Override
    public Path save(Path path, String easefileAsString) {
        isExistCheck(path);
        return FileUtils.fileSave(path.toString(), easefileAsString, false);
    }

    @Override
    public Path update(Path path, String easefileNewContent) {
        isExistCheck(path);
        return FileUtils.fileChange(path.toString(), easefileNewContent);
    }

    @Override
    public boolean delete(Path path) {
        return FileUtils.fileDelete(path.toString());
    }

    private void isExistCheck(Path path) {
        if (FileUtils.isExist(path.toString())) {
            String msg = "Pipeline file just exists for path: " + path;
            logit(WORKSPACE_EVENT, msg);
            throw new IllegalStateException(msg);
        }
    }
}
