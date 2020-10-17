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
        if (!hasAccessRight(path)) {
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
        if (!hasAccessRight(path)) {
            return Tuple.of(path, false, "Access denied");
        }
        if (Files.notExists(path)) {
            Path pathBackward = pathBackward(path);
            if (Files.exists(pathBackward(path))) {
                try {
                    return Tuple.of(Files.createDirectory(path), true, null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return Tuple.of(path, false, "Directory " + pathBackward.toString() + " not exists. Cannot create.");
        }
        return Tuple.of(path, false, "Directory " + path.toString() + " just exists. Cannot create.");
    }

    @Override
    public Tuple2<Boolean, String> deleteDirectory(Path path, boolean force) {
        if (!hasAccessRight(path)) {
            return Tuple.of(false, "Access denied");
        }
        if (Files.isDirectory(path)) {
            if (force) {

                try {
                    org.apache.commons.io.FileUtils.deleteDirectory(path.toFile());
                    return Tuple.of(true, null);
                } catch (IOException e) {
                    e.printStackTrace();
                    return Tuple.of(false, "Exception occurred while trying to force remove directory: " + path.toString());
                }
            } else {
                try {
                    long contentAmounts = Files.list(path).count();
                    if (contentAmounts == 0) {
                        return Tuple.of(Files.deleteIfExists(path), null);
                    } else {
                        return Tuple.of(false, "Cannot remove directory that is not empty. You can use 'force' flag to remove directory with content");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return Tuple.of(false, "Some exception occurred while trying to remove directory: " + path.toString());
                }
            }
        }
        return Tuple.of(false, "Directory not exist or you has no access rights: " + path.toString());
    }

    private boolean hasAccessRight(Path requestedPath) {
        String easefilesStorageLocation = getEasefilesStorageLocationNoSlashAtEnd();
        return requestedPath.toString().startsWith(easefilesStorageLocation) || requestedPath.toString().equals(easefilesStorageLocation);
    }
}