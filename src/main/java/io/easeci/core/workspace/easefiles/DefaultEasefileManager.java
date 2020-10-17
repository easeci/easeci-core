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
import static io.easeci.core.workspace.easefiles.EasefileStatus.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

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
    public EasefileOut load(Path path) {
        if (!hasAccessRight(path)) {
            return EasefileOut.of(EDIT_FAILED, null, "Access denied");
        }
        String content = FileUtils.fileLoad(path.toString());
        EasefileStatus status = EasefileStatus.LOADING_ERROR;
        String errorMessage = null;
        if (content.length() > 0) {
            status = EasefileStatus.CORRECTLY_LOADED;
        } else {
            errorMessage = "Cannot load file, content is empty";
        }
        return EasefileOut.of(status, content, errorMessage);
    }

    @Override
    public EasefileOut save(Path path, String easefileAsString) {
        if (!hasAccessRight(path)) {
            return EasefileOut.of(EDIT_FAILED, null, "Access denied");
        }
        if (!Files.exists(pathBackward(path))) {
            return EasefileOut.of(SAVE_FAILED, null, "Indicated directory for save file not exists");
        } else {
            try {
                isExistCheck(path);
                Path savedPath = FileUtils.fileSave(path.toString(), easefileAsString, false);
                if (nonNull(savedPath)) {
                    EasefileStatus status = Files.exists(savedPath) ? SAVED_CORRECTLY : SAVE_FAILED;
                    return EasefileOut.of(status, null, null, savedPath);
                } else {
                    return EasefileOut.of(SAVE_FAILED, null, "File was not saved");
                }
            } catch (IllegalStateException e) {
                return EasefileOut.of(EasefileStatus.JUST_EXISTS, null, "File just exists: " + path.toString());
            }
        }
    }

    @Override
    public EasefileOut update(Path path, String easefileNewContent) {
        if (!hasAccessRight(path)) {
            return EasefileOut.of(EDIT_FAILED, null, "Access denied");
        }
        if (!Files.exists(path)) {
            return EasefileOut.of(EDIT_FAILED, null, "Requested file path not exists, cannot edit");
        } else {
            Path editedPath = FileUtils.fileChange(path.toString(), easefileNewContent);
            if (nonNull(editedPath) && editedPath.equals(path)) {
                EasefileStatus status = Files.exists(editedPath) ? EDITED_CORRECTLY : EDIT_FAILED;
                return EasefileOut.of(status, null, null, editedPath);
            } else {
                return EasefileOut.of(EDIT_FAILED, null, "File was not edited");
            }
        }
    }

    @Override
    public boolean delete(Path path) {
        if (!hasAccessRight(path)) {
            return false;
        }
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
