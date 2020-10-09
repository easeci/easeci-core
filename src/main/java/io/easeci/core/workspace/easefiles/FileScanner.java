package io.easeci.core.workspace.easefiles;

import io.easeci.core.workspace.easefiles.filetree.FileTree;

import java.nio.file.Path;

/**
 * Scan file structure in order to get the view
 * of file tree in specific path.
 * @author Karol Meksuła
 * 2020-10-06
 * */
public interface FileScanner {

    /**
     * Use this method to scan and list all Easefiles recursively in whole workspace.
     * This method gives us view of whole file tree.
     * @return FileTree that contains all directories and pipeline Easefiles stored in workspace
     * */
    FileTree scan();

    /**
     * Use this method to scan path passed in argument without nested directories (no recursion).
     * This method gives us view of one location without file tree's branching.
     * @return FileTree that contains all directories and pipeline Easefiles stored in workspace
     * */
    FileTree scan(Path path);
}
