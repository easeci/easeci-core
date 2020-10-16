package io.easeci.core.workspace.easefiles;

import io.easeci.commons.FileUtils;
import io.easeci.core.workspace.easefiles.filetree.FileTree;
import io.easeci.core.workspace.easefiles.filetree.FileTreeWalker;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelName.WORKSPACE_EVENT;
import static io.easeci.core.log.ApplicationLevelLogFacade.logit;
import static io.easeci.core.workspace.LocationUtils.getEasefilesStorageLocation;
import static io.easeci.core.workspace.LocationUtils.getEasefilesStorageLocationNoSlashAtEnd;
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
        String easefilesStorageLocation = getEasefilesStorageLocationNoSlashAtEnd();
        if ((!path.toString().startsWith(easefilesStorageLocation)) || !path.toString().equals(easefilesStorageLocation)) {
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

    @Override
    public Tuple3<Path, Boolean, String> createDirectory(Path path) {
        if (Files.notExists(path)) {
            Path pathBackward = pathBackward(path);
            if (Files.exists(pathBackward(path))) {
                try {
                    return Tuple.of(Files.createDirectory(path), true, "");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return Tuple.of(path, false, "Directory " + pathBackward.toString() + " not exists. Cannot create.");
        }
        return Tuple.of(path, false, "Directory " + path.toString() + " just exists. Cannot create.");
    }

    @Override
    public boolean deleteDirectory(Path path, boolean force) {
        return false;
    }
}
