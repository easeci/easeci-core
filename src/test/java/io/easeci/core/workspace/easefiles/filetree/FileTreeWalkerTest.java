package io.easeci.core.workspace.easefiles.filetree;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileTreeWalkerTest {
    private final static String TEST_DIR = "/src/test/java";

    @Test
    @DisplayName("Should get complete file tree of test directory")
    void dumpAllTest() throws IOException {
        Path testDirectory = getTestDirPath();

        FileTreeWalker fileTreeWalker = new FileTreeWalker(testDirectory);
        FileTree fileTree = fileTreeWalker.dumpAll();
        Node rootNode = fileTree.getRootNode();

        assertAll(() -> assertNotNull(fileTreeWalker),
                () -> assertNotNull(fileTree),
                () -> assertNotNull(fileTree.getEntryPoint()),
                () -> assertEquals(fileTree.getEntryPoint(), getTestDirPath()),
                () -> assertNotNull(rootNode),
                () -> assertTrue(rootNode.hasNext()),
                () -> assertEquals(2, rootNode.getChildNodes().size()),
                () -> assertEquals(NodeType.DIRECTORY, rootNode.getNodeType()),
                () -> assertEquals(1, rootNode.getChildNodes().get(0).getChildNodes().size()),
                () -> assertEquals(NodeType.DIRECTORY, rootNode.getChildNodes().get(0).getChildNodes().get(0).getNodeType()),
                () -> assertTrue(rootNode.getChildNodes().get(0).getChildNodes().get(0).hasNext()),
                () -> assertEquals(Path.of(System.getProperty("user.dir") + "/src/test/java/commons/WorkspaceTestUtils.java"),
                                    rootNode.getChildNodes().get(1).getChildNodes().get(0).getNodePath()));
    }

    @Test
    @DisplayName("Should get structure of one directory, not a files tree")
    void dumpOneTest() throws IOException {
        Path testDirectory = getTestDirPath();

        FileTreeWalker fileTreeWalker = new FileTreeWalker(testDirectory);
        FileTree fileTree = fileTreeWalker.dumpOne();
        Node rootNode = fileTree.getRootNode();

        assertAll(() -> assertNotNull(fileTreeWalker),
                () -> assertNotNull(fileTree),
                () -> assertNotNull(fileTree.getEntryPoint()),
                () -> assertEquals(fileTree.getEntryPoint(), getTestDirPath()),
                () -> assertNotNull(rootNode),
                () -> assertTrue(rootNode.hasNext()),
                () -> assertEquals(2, rootNode.getChildNodes().size()),
                () -> assertEquals(NodeType.DIRECTORY, rootNode.getNodeType()),
                () -> assertEquals(NodeType.DIRECTORY, rootNode.getNodeType()),
                () -> assertTrue(rootNode.getChildNodes().get(0).hasNext()),
                () -> assertTrue(rootNode.getChildNodes().get(1).hasNext()),
                // Why above hasNext() == true,
                // and childNodes.isEmpty() == true??
                // Because dumpOne() method not go to nested directories!
                // If we know that nested directories at some path exists,
                // we can next invoke dumpOne() method at this nested directory
                () -> assertTrue(rootNode.getChildNodes().get(0).getChildNodes().isEmpty()),
                () -> assertTrue(rootNode.getChildNodes().get(1).getChildNodes().isEmpty()));
    }

    @Test
    @DisplayName("Should return empty FileTree when we pass as an argument not directory but file for example")
    void dumpAllEmptyTest() throws IOException {
        Path testDirectory = Paths.get(getTestDirPath().toString().concat("/commons/WorkspaceTestUtils.java"));

        FileTreeWalker fileTreeWalker = new FileTreeWalker(testDirectory);
        FileTree fileTree = fileTreeWalker.dumpOne();
        Node rootNode = fileTree.getRootNode();

        assertAll(() -> assertNotNull(fileTreeWalker),
                () -> assertNotNull(fileTree),
                () -> assertNotNull(fileTree.getEntryPoint()),
                () -> assertEquals(fileTree.getEntryPoint(), testDirectory),
                () -> assertNotNull(rootNode),
                () -> assertFalse(rootNode.hasNext()),
                () -> assertNull(rootNode.getChildNodes()),
                () -> assertEquals(NodeType.FILE, rootNode.getNodeType()));
    }

    @Test
    @DisplayName("Should return only root node without children when we pass empty directory")
    void dumpAllEmptyDirTest() throws IOException {
        Path path = Paths.get("/tmp/testdir");
        Files.deleteIfExists(path);
        Path directoryPath = Files.createDirectory(path);

        FileTreeWalker fileTreeWalker = new FileTreeWalker(directoryPath);
        FileTree fileTree = fileTreeWalker.dumpOne();
        Node rootNode = fileTree.getRootNode();

        System.out.println(fileTree.jsonify());

        assertAll(() -> assertNotNull(fileTreeWalker),
                () -> assertNotNull(fileTree),
                () -> assertNotNull(fileTree.getEntryPoint()),
                () -> assertEquals(fileTree.getEntryPoint(), directoryPath),
                () -> assertNotNull(rootNode),
                () -> assertFalse(rootNode.hasNext()),
                () -> assertTrue(rootNode.getChildNodes().isEmpty()),
                () -> assertEquals(NodeType.DIRECTORY, rootNode.getNodeType()));

        Files.deleteIfExists(directoryPath);
    }

    @Test
    @DisplayName("Should return file tree without any nodes when we pass not existing path")
    void dumpAllNotExistingPathTest() throws IOException {
        Path directoryPath = Paths.get("/tmp/not-existing/path");

        FileTreeWalker fileTreeWalker = new FileTreeWalker(directoryPath);
        FileTree fileTree = fileTreeWalker.dumpOne();

        assertAll(() -> assertNotNull(fileTree),
                () -> assertNull(fileTree.getRootNode()),
                () -> assertEquals(directoryPath, fileTree.getEntryPoint()));
    }

    @Test
    @DisplayName("Should display all next locations of path that we typed")
    void nextLocationTest() throws IOException {
        Path testDirectory = getTestDirPath();

        FileTreeWalker fileTreeWalker = new FileTreeWalker(testDirectory);
        FileTree fileTree = fileTreeWalker.dumpAll();

        List<Path> pathsOfThee = fileTree.nextLocations();
        List<Path> paths = fileTree.getRootNode().nextLocations();

        assertEquals(2, paths.size());
        assertEquals(paths, pathsOfThee);
    }

    @Test
    @DisplayName("User story example: User want to walk through directory structure")
    void test() throws IOException {
        Path testDirectory = getTestDirPath();

        FileTreeWalker fileTreeWalker = new FileTreeWalker(testDirectory);

        // First root directory
        FileTree fileTree = fileTreeWalker.dumpOne();
        String firstJson = fileTree.jsonify();
        System.out.println(firstJson);

        // User wants to move forward to next directory
        // For example to /io directory
        Path nextPath = Paths.get(testDirectory.toString() + "/io");
        FileTreeWalker fileTreeWalkerNext = new FileTreeWalker(nextPath);
        FileTree fileTreeNext = fileTreeWalkerNext.dumpOne();
        String secondJson = fileTreeNext.jsonify();
        System.out.println(secondJson);

        // and then user can recursively walk through whole structure...
    }

    private Path getTestDirPath() {
        String pwd = System.getProperty("user.dir");
        return Path.of(pwd.concat(TEST_DIR));
    }
}