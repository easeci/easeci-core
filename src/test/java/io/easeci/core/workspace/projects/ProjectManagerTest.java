package io.easeci.core.workspace.projects;

import io.easeci.core.bootstrap.BootstrapperFactory;
import io.easeci.core.engine.pipeline.Pipeline;
import io.easeci.core.extension.PluginSystemCriticalException;
import io.easeci.core.workspace.projects.dto.AddProjectGroupRequest;
import io.easeci.core.workspace.projects.dto.AddProjectRequest;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.UUID;

import static io.easeci.core.workspace.LocationUtils.getProjectsStructureFileLocation;
import static io.easeci.core.workspace.projects.ProjectsFile.defaultProjectGroupId;
import static io.easeci.core.workspace.projects.ProjectsFile.defaultProjectId;
import static io.easeci.core.workspace.projects.Utils.*;
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

        boolean isTagChanged = pipelinePointerIO.changePipelinePointerTag(projectId, pipelinePointerId, newTagName);

        PipelinePointer pipelinePointerChanged = firstPipelinePointer(projectsFile);

        assertAll(() -> assertTrue(isTagChanged),
                () -> assertEquals(newTagName, pipelinePointerChanged.getTag()),
                () -> assertNotEquals(oldTagName, pipelinePointerChanged.getTag()));
    }

    @Test
    @DisplayName("Should correctly change description of pipeline pointer when one just exists")
    void changeDescriptionOfPipelinePointerSuccessTest() {
        PipelinePointerIO pipelinePointerIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();
        Pipeline.Metadata pipelineMeta = preparePipelineMetadata();
        boolean isPipelinePointerCreated = pipelinePointerIO.createNewPipelinePointer(pipelineMeta);
        assertTrue(isPipelinePointerCreated);

        final String oldDescription = pipelineMeta.getDescription();
        final String newDescription = "This is new description of this pipeline.";
        final Long pipelinePointerId = 0L;
        final Long projectId = 0L;

        boolean isDescriptionChanged = pipelinePointerIO.changePipelinePointerDescription(projectId, pipelinePointerId, newDescription);

        PipelinePointer pipelinePointerChanged = firstPipelinePointer(projectsFile);

        assertAll(() -> assertTrue(isDescriptionChanged),
                () -> assertEquals(newDescription, pipelinePointerChanged.getDescription()),
                () -> assertNotEquals(oldDescription, pipelinePointerChanged.getDescription()));
    }

    @Test
    @DisplayName("Should correctly add new project and assign this one to 'other' - default project group")
    void createProjectSuccessTest() {
        ProjectIO projectIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();

        Long defaultProjectGroupId = defaultProjectGroupId();
        AddProjectRequest addProjectRequest = prepareAddProjectRequest(defaultProjectGroupId);
        boolean isProjectAdded = projectIO.createNewProject(addProjectRequest);

        assertAll(() -> assertNotNull(projectIO),
                () -> assertNotNull(projectsFile),
                () -> assertTrue(isProjectAdded));

        Project justAddedProject = firstAddedProject(projectsFile);

        assertAll(() -> assertEquals(1, justAddedProject.getId()),
                () -> assertNotNull(justAddedProject.getCratedDate()),
                () -> assertNull(justAddedProject.getLastModifiedDate()),
                () -> assertEquals(addProjectRequest.getName(), justAddedProject.getName()),
                () -> assertEquals(addProjectRequest.getTag(), justAddedProject.getTag()),
                () -> assertEquals(addProjectRequest.getDescription(), justAddedProject.getDescription()),
                () -> assertEquals(0, justAddedProject.getPipelines().size())
        );
    }

    @Test
    @DisplayName("Should not add new project because one with such name just exists")
    void createProjectNameExistsTest() {
        ProjectIO projectIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();

        ProjectGroup projectGroup = new ProjectGroup();
        projectGroup.setId(1L);
        projectGroup.setName("Some Project group");
        projectGroup.setProjects(new ArrayList<>(1));
        projectsFile.getProjectGroups().add(projectGroup);

        AddProjectRequest addProjectRequest = prepareAddProjectRequest(projectGroup.getId());

        // first adding
        projectIO.createNewProject(addProjectRequest);

        // second adding
        assertThrows(PipelineManagementException.class, () -> projectIO.createNewProject(addProjectRequest));
    }

    @Test
    @DisplayName("Should not add new project because project group not exists")
    void createProjectNotExistsTest() {
        ProjectIO projectIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();

        Long notExistingProjectGroup = 140L;
        AddProjectRequest addProjectRequest = prepareAddProjectRequest(notExistingProjectGroup);

        assertThrows(PipelineManagementException.class, () -> projectIO.createNewProject(addProjectRequest));
    }

    @Test
    @DisplayName("Should correctly softly remove project if one exists")
    void softRemoveProjectSuccessTest() {
        ProjectIO projectIO = ProjectManager.getInstance();
        PipelinePointerIO pipelinePointerIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();

        Long defaultProjectGroupId = defaultProjectGroupId();
        AddProjectRequest addProjectRequest = prepareAddProjectRequest(defaultProjectGroupId);
        projectIO.createNewProject(addProjectRequest);

        Project projectToRemove = firstAddedProject(projectsFile);
        Pipeline.Metadata pipelineMeta = preparePipelineMetadata();
        pipelineMeta.setProjectId(projectToRemove.getId());
        pipelinePointerIO.createNewPipelinePointer(pipelineMeta);

        // In soft removal this pipeline must not be deleted!
        // This pipelinePointer must be moved to 'other' named project with id: 0
        PipelinePointer pipelinePointer = projectToRemove.getPipelines().get(0);

        assertAll(
                // 2 project should be in projectGroup at index = 0
                () -> assertEquals(2, projectsFile.getProjectGroups().get(0).getProjects().size()),
                // First default project should has any pipelinePointers
                () -> assertEquals(0, projectsFile.getProjectGroups().get(0).getProjects().get(0).getPipelines().size()),
                // Second project should has one just added pipelinePointer
                () -> assertEquals(1, projectsFile.getProjectGroups().get(0).getProjects().get(1).getPipelines().size())
        );

        boolean isRemoved = projectIO.deleteProject(defaultProjectGroupId, projectToRemove.getId(), false);

        assertAll(
                () -> assertTrue(isRemoved),
                // 1 project should be in projectGroup at index = 0, it was 2 project and 1 was removed
                () -> assertEquals(1, projectsFile.getProjectGroups().get(0).getProjects().size()),
                // First default project had 0 pipelinePointer but now has 1,
                // because it was moved from softly deleted project
                () -> assertEquals(1, projectsFile.getProjectGroups().get(0).getProjects().get(0).getPipelines().size()),
                // Second project now not exists because it was removed
                () -> assertThrows(IndexOutOfBoundsException.class, () -> projectsFile.getProjectGroups().get(0).getProjects().get(1).getPipelines()),
                () -> assertEquals(pipelinePointer.getPipelineId(), projectsFile.getProjectGroups().get(0).getProjects().get(0).getPipelines().get(0).getPipelineId())
        );
    }

    @Test
    @DisplayName("Should correctly hard(cascade: project with pipelinePointer) remove project if one exists")
    void hardRemoveProjectSuccessTest() {
        ProjectIO projectIO = ProjectManager.getInstance();
        PipelinePointerIO pipelinePointerIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();

        Long defaultProjectGroupId = defaultProjectGroupId();
        AddProjectRequest addProjectRequest = prepareAddProjectRequest(defaultProjectGroupId);
        projectIO.createNewProject(addProjectRequest);

        Project projectToRemove = firstAddedProject(projectsFile);
        Pipeline.Metadata pipelineMeta = preparePipelineMetadata();
        pipelineMeta.setProjectId(projectToRemove.getId());
        pipelinePointerIO.createNewPipelinePointer(pipelineMeta);

        // In soft removal this pipeline must not be deleted!
        // This pipelinePointer must be moved to 'other' named project with id: 0
        PipelinePointer pipelinePointer = projectToRemove.getPipelines().get(0);

        assertAll(
                // 2 project should be in projectGroup at index = 0
                () -> assertEquals(2, projectsFile.getProjectGroups().get(0).getProjects().size()),
                // First default project should has any pipelinePointers
                () -> assertEquals(0, projectsFile.getProjectGroups().get(0).getProjects().get(0).getPipelines().size()),
                // Second project should has one just added pipelinePointer
                () -> assertEquals(1, projectsFile.getProjectGroups().get(0).getProjects().get(1).getPipelines().size())
        );

        boolean isRemoved = projectIO.deleteProject(defaultProjectGroupId, projectToRemove.getId(), true);

        assertAll(
                () -> assertTrue(isRemoved),
                // 1 project should be in projectGroup at index = 0, it was 2 project and 1 was removed
                () -> assertEquals(1, projectsFile.getProjectGroups().get(0).getProjects().size()),
                // First default project had 0 pipelinePointer and now has 0 too (hard/cascade removal),
                () -> assertEquals(0, projectsFile.getProjectGroups().get(0).getProjects().get(0).getPipelines().size()),
                // Second project now not exists because it was removed
                () -> assertThrows(IndexOutOfBoundsException.class, () -> projectsFile.getProjectGroups().get(0).getProjects().get(1).getPipelines())
        );
    }

    @Test
    @DisplayName("Should not remove project when one not exists")
    void removeProjectNotExistsTest() {
        ProjectIO projectIO = ProjectManager.getInstance();
        PipelinePointerIO pipelinePointerIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();

        final Long notExistingProjectId = 140L;

        assertThrows(PipelineManagementException.class, () -> projectIO.deleteProject(defaultProjectGroupId(), notExistingProjectId, true));
    }

    @Test
    @DisplayName("Should cannot remove DEFAULT project")
    void removeProjectDeniedTest() {
        ProjectIO projectIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();
        Long defaultProjectGroupId = defaultProjectGroupId();

        boolean isProjectRemoved = projectIO.deleteProject(defaultProjectGroupId, defaultProjectId(), true);

        assertFalse(isProjectRemoved);
        assertEquals(1, projectsFile.getProjectGroups().get(0).getProjects().size());
    }

    @Test
    @DisplayName("Should correctly rename project")
    void renameProjectSuccessTest() {
        ProjectIO projectIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();

        Long defaultProjectGroupId = defaultProjectGroupId();
        AddProjectRequest addProjectRequest = prepareAddProjectRequest(defaultProjectGroupId);
        boolean isProjectAdded = projectIO.createNewProject(addProjectRequest);

        assertAll(() -> assertNotNull(projectIO),
                () -> assertNotNull(projectsFile),
                () -> assertTrue(isProjectAdded));

        Project justAddedProject = firstAddedProject(projectsFile);
        final String projectNewName = "Updated project name";

        boolean isRenamed = projectIO.renameProject(justAddedProject.getId(), projectNewName);

        assertAll(() -> assertTrue(isRenamed),
                () -> assertEquals(projectNewName, justAddedProject.getName()));
    }

    @Test
    @DisplayName("Should correctly change tag of project")
    void changeProjectTagSuccessTest() {
        ProjectIO projectIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();

        Long defaultProjectGroupId = defaultProjectGroupId();
        AddProjectRequest addProjectRequest = prepareAddProjectRequest(defaultProjectGroupId);
        boolean isProjectAdded = projectIO.createNewProject(addProjectRequest);

        assertAll(() -> assertNotNull(projectIO),
                () -> assertNotNull(projectsFile),
                () -> assertTrue(isProjectAdded));

        Project justAddedProject = firstAddedProject(projectsFile);
        final String projectNewTag = "Production mode";

        boolean isRenamed = projectIO.changeProjectTag(justAddedProject.getId(), projectNewTag);

        assertAll(() -> assertTrue(isRenamed),
                () -> assertEquals(projectNewTag, justAddedProject.getTag()));
    }

    @Test
    @DisplayName("Should correctly change description of project")
    void changeProjectDescriptionSuccessTest() {
        ProjectIO projectIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();

        Long defaultProjectGroupId = defaultProjectGroupId();
        AddProjectRequest addProjectRequest = prepareAddProjectRequest(defaultProjectGroupId);
        boolean isProjectAdded = projectIO.createNewProject(addProjectRequest);

        assertAll(() -> assertNotNull(projectIO),
                () -> assertNotNull(projectsFile),
                () -> assertTrue(isProjectAdded));

        Project justAddedProject = firstAddedProject(projectsFile);
        final String projectNewDescription = "Production mode description";

        boolean isRenamed = projectIO.changeProjectDescription(justAddedProject.getId(), projectNewDescription);

        assertAll(() -> assertTrue(isRenamed),
                () -> assertEquals(projectNewDescription, justAddedProject.getDescription()));
    }

    @Test
    @DisplayName("Should correctly add project group")
    void createProjectGroupTest() {
        ProjectGroupIO projectGroupIO = ProjectManager.getInstance();
        AddProjectGroupRequest request = prepareAddProjectGroupRequest();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();

        ProjectGroup newProjectGroupJustAdded = projectGroupIO.createNewProjectGroup(request);

        assertAll(() -> assertEquals(request.getName(), newProjectGroupJustAdded.getName()),
                () -> assertEquals(request.getTag(), newProjectGroupJustAdded.getTag()),
                () -> assertEquals(request.getDescription(), newProjectGroupJustAdded.getDescription()));

        // id of new added group
        Long projectGroupId = newProjectGroupJustAdded.getId();
        ProjectGroup projectGroup = firstProjectGroup(projectsFile, projectGroupId);

        assertAll(() -> assertEquals(1, projectGroupId),
                () -> assertEquals(request.getName(), projectGroup.getName()),
                () -> assertEquals(request.getTag(), projectGroup.getTag()),
                () -> assertEquals(request.getDescription(), projectGroup.getDescription()),
                () -> assertEquals(0, projectGroup.getProjects().size()));
    }

    @Test
    @DisplayName("Should not add project group because one exists with same name")
    void createProjectGroupJustExistsTest() {
        ProjectGroupIO projectGroupIO = ProjectManager.getInstance();
        AddProjectGroupRequest request = prepareAddProjectGroupRequest();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();

        // add a first time
        ProjectGroup newProjectGroup = projectGroupIO.createNewProjectGroup(request);

        // add a second time
        assertAll(() -> assertThrows(PipelineManagementException.class, () -> projectGroupIO.createNewProjectGroup(request)),
                () -> assertEquals(2, projectsFile.getProjectGroups().size()));
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

    private Project firstAddedProject(ProjectsFile projectsFile) {
        return projectsFile.getProjectGroups()
                .get(0)
                .getProjects()
                .get(1); // get first project in projectGroup, not zero because on zero index is default project
    }

    private ProjectGroup firstProjectGroup(ProjectsFile projectsFile, Long projectGroupId) {
        return projectsFile.getProjectGroups().stream()
                .filter(projectGroup -> projectGroup.getId().equals(projectGroupId))
                .findFirst()
                .orElseThrow();
    }

    @AfterAll
    static void cleanup() throws IOException {
        Files.deleteIfExists(getProjectsStructureFileLocation());
    }
}