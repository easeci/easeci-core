package io.easeci.core.workspace.projects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.easeci.commons.DirUtils;
import io.easeci.core.engine.pipeline.Pipeline;
import io.easeci.core.workspace.projects.dto.AddProjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelName.WORKSPACE_EVENT;
import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelPrefix.THREE;
import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelPrefix.TWO;
import static io.easeci.core.log.ApplicationLevelLogFacade.logit;
import static io.easeci.core.workspace.LocationUtils.getProjectsStructureFileLocation;
import static io.easeci.core.workspace.LocationUtils.getWorkspaceLocation;
import static io.easeci.core.workspace.projects.PipelineManagementException.PipelineManagementStatus.*;
import static io.easeci.core.workspace.projects.ProjectUtils.nextPipelinePointerId;
import static io.easeci.core.workspace.projects.ProjectUtils.nextProjectId;
import static io.easeci.core.workspace.projects.ProjectsFile.defaultProjectId;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

public class ProjectManager implements PipelinePointerIO, ProjectIO {
    public final static String PROJECTS_DIRECTORY = "/projects/";
    public final static String PROJECTS_FILE = PROJECTS_DIRECTORY + "projects-structure.json";
    private static ProjectManager projectManager;
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    // todo issue: multithreading can destroy this easily, two another thread could have got another content of file
    private static ProjectsFile projectsFile; // TODO this object must be synchronised in future in case [core #0018]

    private ProjectManager() {
        logit(WORKSPACE_EVENT, "Initialization of projects place in workspace of: " + PROJECTS_FILE, THREE);
        this.initializeDirectory();
        this.initializeProjectsFile();
        try {
            projectsFile = this.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ProjectManager getInstance() {
        if (projectManager == null) {
            ProjectManager.projectManager = new ProjectManager();
            return getInstance();
        }
        return ProjectManager.projectManager;
    }

    private Path initializeDirectory() {
        final String workspaceLocation = getWorkspaceLocation();
        final String projectsDirLocation = workspaceLocation.concat(PROJECTS_DIRECTORY);
        if (!DirUtils.isDirectoryExists(projectsDirLocation)) {
            Path path = DirUtils.directoryCreate(projectsDirLocation);
            logit(WORKSPACE_EVENT, "Directory for projects store just created at here: " + projectsDirLocation, THREE);
            return path;
        }
        return Path.of(projectsDirLocation);
    }

    private Path initializeProjectsFile() {
        final String workspaceLocation = getWorkspaceLocation();
        Path projectsStructureFile = Paths.get(workspaceLocation.concat(PROJECTS_FILE));
        if (Files.exists(projectsStructureFile)) {
            logit(WORKSPACE_EVENT, PROJECTS_FILE + " just exists here: " + projectsStructureFile + ", not created again", THREE);
            return projectsStructureFile;
        }
        try {
            Files.createFile(projectsStructureFile);
            ProjectsFile projectsFile = ProjectsFile.initialState();
            String projectsFileAsString = OBJECT_MAPPER.writeValueAsString(projectsFile);
            Files.writeString(projectsStructureFile, projectsFileAsString);
        } catch (IOException e) {
            e.printStackTrace();
            logit(WORKSPACE_EVENT, "Exception was thrown when trying to create file: '" + PROJECTS_FILE + "'");
            return projectsStructureFile;
        }
        return projectsStructureFile;
    }

    public ProjectsFile getProjectsFile() {
        if (isNull(projectsFile)) {
            try {
                projectsFile = load();
            } catch (IOException e) {
                e.printStackTrace();
                logit(WORKSPACE_EVENT, "Critical error, cannot loaded file: '" + PROJECTS_FILE + "'\nException: " + e.toString(), TWO);
            }
        }
        return projectsFile;
    }

    public ProjectsFile load() throws IOException {
        Path projectsStructureFileLocation = getProjectsStructureFileLocation();
        return OBJECT_MAPPER.readValue(projectsStructureFileLocation.toFile(), ProjectsFile.class);
    }

    @Override
    public boolean createNewPipelinePointer(Pipeline.Metadata pipelineMeta) {
        validate(pipelineMeta);
        PipelinePointer pointer = new PipelinePointer();
        pointer.setId(nextPipelinePointerId(projectsFile));
        pointer.setProjectId(ofNullable(pipelineMeta.getProjectId()).orElse(defaultProjectId()));
        pointer.setPipelineId(pipelineMeta.getPipelineId());
        pointer.setPipelineFilePath(pipelineMeta.getPipelineFilePath());
        pointer.setCreatedDate(pipelineMeta.getCreatedDate());
        pointer.setEasefilePath(pipelineMeta.getEasefilePath());
        pointer.setName(pipelineMeta.getName());
        pointer.setTag(pipelineMeta.getTag());
        pointer.setDescription(pipelineMeta.getDescription());

        boolean isJoined = projectsFile.join(pointer);
        if (isJoined) {
            logit(WORKSPACE_EVENT, "Pipeline called: '" + pipelineMeta.getName() + "' added to project with id: '" + pipelineMeta.getProjectId() + "'", THREE);
            save();
        } else {
            logit(WORKSPACE_EVENT, "Critical error, seems like project with id: '" + pipelineMeta.getProjectId() + "' not exists ?", THREE);
        }
        return isJoined;
    }

    private void validate(Pipeline.Metadata pipelineMeta) {
        final Project project = findProject(pipelineMeta.getProjectId());
        validatePipelinePointer(pipelinePointer -> pipelinePointer.getName().equals(pipelineMeta.getName()),
                                project,
                                PIPELINE_NAME_EXISTS);
        validatePipelinePointer(pipelinePointer -> pipelinePointer.getPipelineId().equals(pipelineMeta.getPipelineId()),
                                project,
                                PIPELINE_ID_EXISTS);
    }

    private void validatePipelinePointer(Predicate<PipelinePointer> predicate, Project project, PipelineManagementException.PipelineManagementStatus status) {
        boolean pipelinePointerExists = project.getPipelines().stream().anyMatch(predicate);
        if (pipelinePointerExists) {
            throw new PipelineManagementException(status);
        }
    }

    private Project findProject(Long projectId) {
        return projectsFile.getProjectGroups().stream()
                           .flatMap(projectGroup -> projectGroup.getProjects().stream())
                           .filter(project -> project.getId().equals(projectId))
                           .findFirst()
                           .orElseThrow(() -> new PipelineManagementException(PROJECT_NOT_EXISTS));
    }

    @Override
    public boolean deletePipelinePointer(Long projectId, Long pipelinePointerId) {
        List<PipelinePointer> pipelinePointers = findProject(projectId).getPipelines();
        PipelinePointer found = pipelinePointers.stream()
                .filter(pipelinePointer -> pipelinePointer.getId().equals(pipelinePointerId))
                .findFirst().orElseThrow(() -> new PipelineManagementException(PIPELINE_NOT_EXISTS));
        boolean isRemoved = pipelinePointers.remove(found);
        if (isRemoved) {
            logit(WORKSPACE_EVENT, "Pipeline Pointer with id: '" + pipelinePointerId + "' was successfully removed");
        }
        return isRemoved;
    }

    @Override
    public boolean renamePipelinePointer(Long projectId, Long pipelinePointerId, String pipelinePointerName) {
        return changeField(projectId, pipelinePointerId,
                pipelinePointer -> {
                    pipelinePointer.setName(pipelinePointerName);
                    logit(WORKSPACE_EVENT, "Changing name of pipeline with id: '"
                            + pipelinePointerId + "', old: '" + pipelinePointer.getName() + "', new: '" + pipelinePointerName + "'");
                });
    }

    @Override
    public boolean changePipelinePointerTag(Long projectId, Long pipelinePointerId, String tagName) {
        return changeField(projectId, pipelinePointerId,
                pipelinePointer -> {
                    pipelinePointer.setTag(tagName);
                    logit(WORKSPACE_EVENT, "Changing tag of pipeline with id: '"
                            + pipelinePointerId + "', old: '" + pipelinePointer.getTag() + "', new: '" + tagName + "'");
                });
    }

    @Override
    public boolean changePipelinePointerDescription(Long projectId, Long pipelinePointerId, String description) {
        return changeField(projectId, pipelinePointerId,
                pipelinePointer -> {
                    pipelinePointer.setDescription(description);
                    logit(WORKSPACE_EVENT, "Changing description of pipeline with id: '"
                            + pipelinePointerId + "', old: '" + pipelinePointer.getDescription() + "', new: '" + description + "'");
                });
    }

    private boolean changeField(Long projectId, Long pipelinePointerId, Consumer<PipelinePointer> fieldSetConsumer) {
        List<PipelinePointer> pipelinePointers = findProject(projectId).getPipelines();
        PipelinePointer found = pipelinePointers.stream()
                .filter(pipelinePointer -> pipelinePointer.getId().equals(pipelinePointerId))
                .findFirst().orElseThrow(() -> new PipelineManagementException(PIPELINE_NOT_EXISTS));
        fieldSetConsumer.accept(found);
        save();
        return true;
    }

    private ProjectsFile save() {
        try {
            String fileContent = OBJECT_MAPPER.writeValueAsString(ProjectManager.projectsFile);
            try {
                Files.writeString(getProjectsStructureFileLocation(), fileContent);
            } catch (IOException e) {
                e.printStackTrace();
                logit(WORKSPACE_EVENT, "IOException occurred while trying to save " + PROJECTS_FILE, THREE);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return ProjectManager.projectsFile;
    }

    @Override
    public boolean createNewProject(AddProjectRequest request) {
        Project project = Project.builder()
                .id(nextProjectId(projectsFile))
                .cratedDate(new Date())
                .name(request.getName())
                .tag(request.getTag())
                .description(request.getDescription())
                .pipelines(new ArrayList<>(0))
                .build();

        ProjectGroup projectGroup = assignProjectGroup(request);
        validateProject(projectGroup, project);
        boolean isAdded = projectGroup.getProjects().add(project);
        if (isAdded) {
            logit(WORKSPACE_EVENT, "New project named: '" +
                    project.getName() + "', with id: '" + project.getId() + "', assigned to projectGroup: '" + projectGroup.getId() + "'");
            save();
        }
        return isAdded;
    }

    private ProjectGroup assignProjectGroup(AddProjectRequest request) {
        if (nonNull(request.getProjectGroupId())) {
            return findProjectGroup(request.getProjectGroupId());
        } else {
            Long otherProjectGroupId = ProjectsFile.defaultProjectGroupId();
            return findProjectGroup(otherProjectGroupId);
        }
    }

    private ProjectGroup findProjectGroup(Long projectGroupId) {
        return projectsFile.getProjectGroups().stream()
                .filter(projectGroup -> projectGroup.getId().equals(projectGroupId))
                .findFirst()
                .orElseThrow(() -> new PipelineManagementException(PROJECT_GROUP_NOT_EXISTS));
    }

    private void validateProject(ProjectGroup group, Project project) {
        boolean isProjectNameExists = group.getProjects().stream().anyMatch(found -> found.getName().equals(project.getName()));
        boolean isProjectIdExists = group.getProjects().stream().anyMatch(found -> found.getId().equals(project.getId()));
        if (isProjectNameExists) {
            logit(WORKSPACE_EVENT, "Could not create new project because one named: '" + project.getName() + "' just exists", THREE);
            throw new PipelineManagementException(PROJECT_NAME_EXISTS);
        }
        if (isProjectIdExists) {
            logit(WORKSPACE_EVENT, "Could not create new project because one with id: '" + project.getId() + "' just exists", THREE);
            throw new PipelineManagementException(PROJECT_ID_EXISTS);
        }
    }

    @Override
    public boolean deleteProject(Long projectGroupId, Long projectId, boolean isHardRemoval) {
        // check if user not trying default secured project
        if (projectId.equals(defaultProjectId())) {
            logit(WORKSPACE_EVENT, "Cannot remove secured project");
            return false;
        }
        boolean isRemoved;
        ProjectGroup projectGroup = findProjectGroup(projectGroupId);
        Project projectToRemoval = projectGroup.getProjects().stream()
                                               .filter(project -> project.getId().equals(projectId))
                                               .findFirst()
                                               .orElseThrow(() -> new PipelineManagementException(PROJECT_NOT_EXISTS));
        if (!isHardRemoval) {
            Project defaultProject = projectGroup.getProjects().stream()
                    .filter(project -> project.getId().equals(defaultProjectId()))
                    .findFirst()
                    .orElseThrow(() -> new PipelineManagementException(PROJECT_NOT_EXISTS));
            movePipelinePointers(projectToRemoval.getPipelines(), defaultProject);
        }
        isRemoved = projectGroup.getProjects().remove(projectToRemoval);
        save();
        return isRemoved;
    }

    private void movePipelinePointers(List<PipelinePointer> pipelinePointers, Project targetProject) {
        pipelinePointers.forEach(pipelinePointer -> pipelinePointer.setProjectId(targetProject.getId()));
        targetProject.getPipelines().addAll(pipelinePointers);
    }

    @Override
    public boolean renameProject(Long projectId, String projectName) {
        findProject(projectId).setName(projectName);
        save();
        return true;
    }

    @Override
    public boolean changeProjectTag(Long projectId, String projectTag) {
        findProject(projectId).setTag(projectTag);
        save();
        return true;
    }

    @Override
    public boolean changeProjectDescription(Long projectId, String projectDescription) {
        findProject(projectId).setDescription(projectDescription);
        save();
        return true;
    }

    static void refreshFileContext() {
        ProjectManager.projectManager = null;
        ProjectManager.getInstance();
    }
}
