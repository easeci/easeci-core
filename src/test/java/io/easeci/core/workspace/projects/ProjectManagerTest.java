package io.easeci.core.workspace.projects;

import io.easeci.core.bootstrap.BootstrapperFactory;
import io.easeci.core.engine.pipeline.Pipeline;
import io.easeci.core.extension.PluginSystemCriticalException;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import static io.easeci.core.workspace.LocationUtils.getProjectsStructureFileLocation;
import static io.easeci.core.workspace.projects.Utils.preparePipelineMetadata;
import static org.junit.jupiter.api.Assertions.*;

class ProjectManagerTest {

    @BeforeEach
    void setup() {
        try {
            BootstrapperFactory.factorize().bootstrap(new String[]{});
            Files.deleteIfExists(getProjectsStructureFileLocation());
            // singleton trap!
            // remember in future: if you are using singleton object it will be one instance object per all test class invocation
            // In order to fix this singleton issue remove one and recreate new object
            ProjectManager.refreshFileContext();
        } catch (IOException | PluginSystemCriticalException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Should correctly add pipeline pointer to 'other' project")
    void createPipelinePointer() {
        // prepare required objects
        PipelinePointerIO pipelinePointerIO = ProjectManager.getInstance();
        Pipeline.Metadata pipelineMeta = preparePipelineMetadata();

        // execute testing method
        boolean isPipelinePointerCreated = pipelinePointerIO.createNewPipelinePointer(pipelineMeta);

        // find just saved pipeline
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();
        PipelinePointer justAddedPointer = firstPipelinePointer(projectsFile);

        assertAll(() -> assertNotNull(pipelinePointerIO),
                  () -> assertNotNull(justAddedPointer),
                  () -> assertTrue(isPipelinePointerCreated),
                  () -> assertEquals(pipelineMeta.getPipelineId(), justAddedPointer.getPipelineId()),
                  () -> assertEquals(pipelineMeta.getCreatedDate(), justAddedPointer.getCreatedDate()),
                  () -> assertEquals(pipelineMeta.getEasefilePath(), justAddedPointer.getEasefilePath()),
                  () -> assertEquals(pipelineMeta.getName(), justAddedPointer.getName()),
                  () -> assertEquals(pipelineMeta.getPipelineFilePath(), justAddedPointer.getPipelineFilePath()),
                  () -> assertEquals(pipelineMeta.getTag(), justAddedPointer.getTag()),
                  () -> assertEquals(pipelineMeta.getProjectId(), justAddedPointer.getProjectId()));
    }

    @Test
    @DisplayName("Should cannot add pipeline with same id twice")
    void createPipelinePointerIdExists() {
        // prepare required objects
        PipelinePointerIO pipelinePointerIO = ProjectManager.getInstance();
        Pipeline.Metadata pipelineMeta = preparePipelineMetadata();
        pipelineMeta.setName("Another test name");

        // execute testing method
        boolean isPipelinePointerCreated = pipelinePointerIO.createNewPipelinePointer(pipelineMeta);

        assertThrows(PipelineManagementException.class, () -> pipelinePointerIO.createNewPipelinePointer(pipelineMeta));

        // find just saved pipeline
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();
        int pipelinesAmount = pipelinesAmount(projectsFile);

        assertAll(() -> assertTrue(isPipelinePointerCreated),
                  () -> assertEquals(1, pipelinesAmount));
    }

    @Test
    @DisplayName("Should cannot add pipeline with just existing name to project")
    void createPipelinePointerNameExists() {
        // prepare required objects
        PipelinePointerIO pipelinePointerIO = ProjectManager.getInstance();
        Pipeline.Metadata pipelineMeta = preparePipelineMetadata();
        pipelineMeta.setPipelineId(UUID.randomUUID());

        // execute testing method
        boolean isPipelinePointerCreated = pipelinePointerIO.createNewPipelinePointer(pipelineMeta);

        assertThrows(PipelineManagementException.class, () -> pipelinePointerIO.createNewPipelinePointer(pipelineMeta));

        // find just saved pipeline
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();
        int pipelinesAmount = pipelinesAmount(projectsFile);

        assertAll(() -> assertTrue(isPipelinePointerCreated),
                  () -> assertEquals(1, pipelinesAmount));
    }

    @Test
    @DisplayName("Should remove correctly pipeline pointer if exists")
    void deletePipelinePointerSuccess() {
        PipelinePointerIO pipelinePointerIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();
        Pipeline.Metadata pipelineMeta = preparePipelineMetadata();
        boolean isPipelinePointerCreated = pipelinePointerIO.createNewPipelinePointer(pipelineMeta);
        assertTrue(isPipelinePointerCreated);

        Long projectId = pipelineMeta.getProjectId();
        Long pipelineId = 0L;

        int pipelinesAmountBeforeRemoval = pipelinesAmount(projectsFile);
        boolean removalResult = pipelinePointerIO.deletePipelinePointer(projectId, pipelineId);
        int pipelinesAmountAfterRemoval = pipelinesAmount(projectsFile);

        assertAll(() -> assertTrue(removalResult),
                  () -> assertEquals(1, pipelinesAmountBeforeRemoval),
                  () -> assertEquals(0, pipelinesAmountAfterRemoval));
    }

    @Test
    @DisplayName("Should not remove pipeline pointer if not exist and should return appropriate error code")
    void deletePipelinePointerNotExists() {
        PipelinePointerIO pipelinePointerIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();
        Pipeline.Metadata pipelineMeta = preparePipelineMetadata();
        boolean isPipelinePointerCreated = pipelinePointerIO.createNewPipelinePointer(pipelineMeta);
        assertTrue(isPipelinePointerCreated);

        Long projectId = pipelineMeta.getProjectId();
        Long pipelineId = 10L; // not existing pipeline pointer

        int pipelinesAmountBeforeRemoval = pipelinesAmount(projectsFile);
        assertAll(() -> assertThrows(PipelineManagementException.class, () -> pipelinePointerIO.deletePipelinePointer(projectId, pipelineId)),
                  () -> {
                      int pipelinesAmountAfterRemoval = pipelinesAmount(projectsFile);
                      assertEquals(1, pipelinesAmountBeforeRemoval);
                      assertEquals(1, pipelinesAmountAfterRemoval);
                  });
    }

    @Test
    @DisplayName("Should not remove pipeline pointer when project not exists")
    void deletePipelinePointerProjectNotExists() {
        PipelinePointerIO pipelinePointerIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();
        Pipeline.Metadata pipelineMeta = preparePipelineMetadata();
        boolean isPipelinePointerCreated = pipelinePointerIO.createNewPipelinePointer(pipelineMeta);
        assertTrue(isPipelinePointerCreated);

        Long projectId = pipelineMeta.getProjectId() + 1;  // not existing project
        Long pipelineId = 0L;

        int pipelinesAmountBeforeRemoval = pipelinesAmount(projectsFile);
        assertAll(() -> assertThrows(PipelineManagementException.class, () -> pipelinePointerIO.deletePipelinePointer(projectId, pipelineId)),
                () -> {
                    int pipelinesAmountAfterRemoval = pipelinesAmount(projectsFile);
                    assertEquals(1, pipelinesAmountBeforeRemoval);
                    assertEquals(1, pipelinesAmountAfterRemoval);
                });
    }

    private int pipelinesAmount(ProjectsFile projectsFile) {
        return projectsFile.getProjectGroups()
                .get(0).getProjects()
                .get(0).getPipelines()
                .size();
    }

    private PipelinePointer firstPipelinePointer(ProjectsFile projectsFile) {
        return projectsFile.getProjectGroups()
                .get(0).getProjects()
                .get(0).getPipelines()
                .get(0);
    }

    @Test
    @DisplayName("Should correctly rename pipeline pointer when one just exists")
    void renamePipelinePointerSuccessTest() {
        PipelinePointerIO pipelinePointerIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();
        Pipeline.Metadata pipelineMeta = preparePipelineMetadata();
        boolean isPipelinePointerCreated = pipelinePointerIO.createNewPipelinePointer(pipelineMeta);
        assertTrue(isPipelinePointerCreated);

        final String oldPipelineName = pipelineMeta.getName();
        final String newPipelineName = "New pipeline name";
        final Long pipelinePointerId = 0L;
        final Long projectId = 0L;

        boolean isRenamed = pipelinePointerIO.renamePipelinePointer(projectId, pipelinePointerId, newPipelineName);

        PipelinePointer pipelinePointerChanged = firstPipelinePointer(projectsFile);

        assertAll(() -> assertTrue(isRenamed),
                () -> assertEquals(newPipelineName, pipelinePointerChanged.getName()),
                () -> assertNotEquals(oldPipelineName, pipelinePointerChanged.getName()));
    }

    @Test
    @DisplayName("Should correctly rename tag of pipeline pointer when one just exists")
    void renameTagOfPipelinePointerSuccessTest() {
        PipelinePointerIO pipelinePointerIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();
        Pipeline.Metadata pipelineMeta = preparePipelineMetadata();
        boolean isPipelinePointerCreated = pipelinePointerIO.createNewPipelinePointer(pipelineMeta);
        assertTrue(isPipelinePointerCreated);

        final String oldTagName = pipelineMeta.getTag();
        final String newTagName = "Tag v2.0";
        final Long pipelinePointerId = 0L;
        final Long projectId = 0L;

        boolean isTagChanged = pipelinePointerIO.changeTag(projectId, pipelinePointerId, newTagName);

        PipelinePointer pipelinePointerChanged = firstPipelinePointer(projectsFile);

        assertAll(() -> assertTrue(isTagChanged),
                () -> assertEquals(newTagName, pipelinePointerChanged.getTag()),
                () -> assertNotEquals(oldTagName, pipelinePointerChanged.getTag()));
    }

    @AfterAll
    static void cleanup() throws IOException {
        Files.deleteIfExists(getProjectsStructureFileLocation());
    }
}