package io.easeci.core.workspace.projects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.easeci.commons.DirUtils;
import io.easeci.core.engine.pipeline.Pipeline;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;

import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelName.WORKSPACE_EVENT;
import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelPrefix.THREE;
import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelPrefix.TWO;
import static io.easeci.core.log.ApplicationLevelLogFacade.logit;
import static io.easeci.core.workspace.LocationUtils.getProjectsStructureFileLocation;
import static io.easeci.core.workspace.LocationUtils.getWorkspaceLocation;
import static io.easeci.core.workspace.projects.PipelineManagementException.PipelineManagementStatus.*;
import static io.easeci.core.workspace.projects.ProjectUtils.nextPipelinePointerId;
import static io.easeci.core.workspace.projects.ProjectsFile.INITIAL_PROJECT_ID;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

public class ProjectManager implements PipelinePointerIO {
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
            logit(WORKSPACE_EVENT, "Exception was thrown when trying to create file: " + PROJECTS_FILE);
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
                logit(WORKSPACE_EVENT, "Critical error, cannot loaded file: " + PROJECTS_FILE + "\nException: " + e.toString(), TWO);
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
        pointer.setProjectId(ofNullable(pipelineMeta.getProjectId()).orElse(INITIAL_PROJECT_ID));
        pointer.setPipelineId(pipelineMeta.getPipelineId());
        pointer.setPipelineFilePath(pipelineMeta.getPipelineFilePath());
        pointer.setCreatedDate(pipelineMeta.getCreatedDate());
        pointer.setEasefilePath(pipelineMeta.getEasefilePath());
        pointer.setName(pipelineMeta.getName());
        pointer.setTag(pipelineMeta.getTag());

        boolean isJoined = projectsFile.join(pointer);
        if (isJoined) {
            logit(WORKSPACE_EVENT, "Pipeline called: " + pipelineMeta.getName() + " added to project with id: " + pipelineMeta.getProjectId(), THREE);
            save();
        } else {
            logit(WORKSPACE_EVENT, "Critical error, seems like project with id: " + pipelineMeta.getProjectId() + " not exists ?", THREE);
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
            logit(WORKSPACE_EVENT, "Pipeline Pointer with id: " + pipelinePointerId + " was successfully removed");
        }
        return isRemoved;
    }

    @Override
    public boolean renamePipelinePointer(Long projectId, Long pipelinePointerId, String pipelinePointerName) {
        List<PipelinePointer> pipelinePointers = findProject(projectId).getPipelines();
        PipelinePointer found = pipelinePointers.stream()
                .filter(pipelinePointer -> pipelinePointer.getId().equals(pipelinePointerId))
                .findFirst().orElseThrow(() -> new PipelineManagementException(PIPELINE_NOT_EXISTS));
        logit(WORKSPACE_EVENT, "Changing name of pipeline with id: "
                + pipelinePointerId + ", old: " + found.getName() + ", new: " + pipelinePointerName);
        found.setName(pipelinePointerName);
        save();
        return true;
    }

    @Override
    public boolean changeTag(Long projectId, Long pipelinePointerId, String tagName) {
        List<PipelinePointer> pipelinePointers = findProject(projectId).getPipelines();
        PipelinePointer found = pipelinePointers.stream()
                .filter(pipelinePointer -> pipelinePointer.getId().equals(pipelinePointerId))
                .findFirst().orElseThrow(() -> new PipelineManagementException(PIPELINE_NOT_EXISTS));
        logit(WORKSPACE_EVENT, "Changing tag of pipeline with id: "
                + pipelinePointerId + ", old: " + found.getTag() + ", new: " + tagName);
        found.setTag(tagName);
        save();
        return true;
    }

    @Override
    public ProjectsFile changeDescription() {
        return null;
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

    static void refreshFileContext() {
        ProjectManager.projectManager = null;
        ProjectManager.getInstance();
    }
}
