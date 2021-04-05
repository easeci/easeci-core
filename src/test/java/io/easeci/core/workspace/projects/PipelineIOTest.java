package io.easeci.core.workspace.projects;

import io.easeci.BaseWorkspaceContextTest;
import io.easeci.core.engine.pipeline.EasefileObjectModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static io.easeci.core.workspace.projects.Utils.provideEmptyPipelineForTest;
import static org.junit.jupiter.api.Assertions.*;

class PipelineIOTest extends BaseWorkspaceContextTest {

    @Test
    @DisplayName("Should correctly create pipeline file")
    void createFileTest() throws IOException {
        ProjectManager.destroyInstance();
        final PipelineIO pipelineIO = ProjectManager.getInstance();

        Path pipelineFile = pipelineIO.createPipelineFile();

        boolean exists = Files.exists(pipelineFile);

        assertTrue(exists);
    }

    @Test
    @DisplayName("Should correctly serialize and save content of EasefileObjectModel to file")
    void savePipelineFileTest() throws IOException {
        ProjectManager.destroyInstance();
        final PipelineIO pipelineIO = ProjectManager.getInstance();

        Path pipelineFile = pipelineIO.createPipelineFile();
        EasefileObjectModel easefileObjectModel = provideEmptyPipelineForTest();
        easefileObjectModel.getMetadata().setPipelineFilePath(pipelineFile);

        Path path = pipelineIO.writePipelineFile(pipelineFile, easefileObjectModel);

        byte[] bytes = Files.readAllBytes(path);

        assertAll(() -> assertTrue(Files.exists(path)),
                  () -> assertEquals(736, bytes.length));
    }

    @Test
    @DisplayName("Should correctly find and read EasefileObjectModel from encoded bytes")
    void readPipelineFileTest() throws IOException {
        ProjectManager.destroyInstance();
        final PipelineIO pipelineIO = ProjectManager.getInstance();
        final PipelinePointerIO pipelinePointerIO = ProjectManager.getInstance();

        Path pipelineFile = pipelineIO.createPipelineFile();
        EasefileObjectModel easefileObjectModel = provideEmptyPipelineForTest();
        easefileObjectModel.getMetadata().setPipelineFilePath(pipelineFile);

        Path path = pipelineIO.writePipelineFile(pipelineFile, easefileObjectModel);
        pipelinePointerIO.createNewPipelinePointer(easefileObjectModel.getMetadata());

        Optional<EasefileObjectModel> easefileObjectModelOptional = pipelineIO.loadPipelineFile(easefileObjectModel.getMetadata().getPipelineId());

        byte[] bytes = Files.readAllBytes(path);

        assertAll(() -> assertTrue(easefileObjectModelOptional.isPresent()),
                  () -> assertTrue(Files.exists(path)),
                  () -> assertEquals(736, bytes.length),
                  () -> assertEquals(easefileObjectModel, easefileObjectModelOptional.orElse(null)));
    }
}