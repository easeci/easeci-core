package io.easeci.core.workspace.projects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.easeci.commons.DirUtils;
import io.easeci.api.projects.dto.AddProjectGroupRequest;
import io.easeci.api.projects.dto.AddProjectRequest;
import io.easeci.core.engine.pipeline.EasefileObjectModel;

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
import static io.easeci.core.workspace.projects.ProjectUtils.*;
import static io.easeci.core.workspace.projects.ProjectsFile.defaultProjectGroupId;
import static io.easeci.core.workspace.projects.ProjectsFile.defaultProjectId;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

public class ProjectManager implements PipelinePointerIO, ProjectIO, ProjectGroupIO {
    public final static String PROJECTS_DIRECTORY = "/projects/";
    public final static String PIPELINES_DIRECTORY = "/projects/pipelines/";
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
        final String pipelinesDirLocation = workspaceLocation.concat(PIPELINES_DIRECTORY);
        if (!DirUtils.isDirectoryExists(projectsDirLocation)) {
            Path path = DirUtils.directoryCreate(projectsDirLocation);
            logit(WORKSPACE_EVENT, "Directory for projects store just created at here: " + projectsDirLocation, THREE);
            if (!DirUtils.isDirectoryExists(pipelinesDirLocation)) {
                DirUtils.directoryCreate(pipelinesDirLocation);
                logit(WORKSPACE_EVENT, "Directory for deserialized pipelines store just created at here: " + projectsDirLocation, THREE);
            }
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

    public List<ProjectGroup> getProjectGroupList() {
        return projectsFile.getProjectGroups();
    }

    @Override
    public PipelinePointer createNewPipelinePointer(EasefileObjectModel.Metadata pipelineMeta) {
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
        return pointer;
    }

    private void validate(EasefileObjectModel.Metadata pipelineMeta) {
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
    public PipelinePointer deletePipelinePointer(Long projectId, Long pipelinePointerId) {
        List<PipelinePointer> pipelinePointers = findProject(projectId).getPipelines();
        PipelinePointer found = pipelinePointers.stream()
                .filter(pipelinePointer -> pipelinePointer.getId().equals(pipelinePointerId))
                .findFirst().orElseThrow(() -> new PipelineManagementException(PIPELINE_NOT_EXISTS));
        boolean isRemoved = pipelinePointers.remove(found);
        if (isRemoved) {
            logit(WORKSPACE_EVENT, "Pipeline Pointer with id: '" + pipelinePointerId + "' was successfully removed");
        }
        return found;
    }

    @Override
    public PipelinePointer renamePipelinePointer(Long projectId, Long pipelinePointerId, String pipelinePointerName) {
        return changeField(projectId, pipelinePointerId,
                pipelinePointer -> {
                    pipelinePointer.setName(pipelinePointerName);
                    logit(WORKSPACE_EVENT, "Changing name of pipeline with id: '"
                            + pipelinePointerId + "', old: '" + pipelinePointer.getName() + "', new: '" + pipelinePointerName + "'");
                });
    }

    @Override
    public PipelinePointer changePipelinePointerTag(Long projectId, Long pipelinePointerId, String tagName) {
        return changeField(projectId, pipelinePointerId,
                pipelinePointer -> {
                    pipelinePointer.setTag(tagName);
                    logit(WORKSPACE_EVENT, "Changing tag of pipeline with id: '"
                            + pipelinePointerId + "', old: '" + pipelinePointer.getTag() + "', new: '" + tagName + "'");
                });
    }

    @Override
    public PipelinePointer changePipelinePointerDescription(Long projectId, Long pipelinePointerId, String description) {
        return changeField(projectId, pipelinePointerId,
                pipelinePointer -> {
                    pipelinePointer.setDescription(description);
                    logit(WORKSPACE_EVENT, "Changing description of pipeline with id: '"
                            + pipelinePointerId + "', old: '" + pipelinePointer.getDescription() + "', new: '" + description + "'");
                });
    }

    private PipelinePointer changeField(Long projectId, Long pipelinePointerId, Consumer<PipelinePointer> fieldSetConsumer) {
        List<PipelinePointer> pipelinePointers = findProject(projectId).getPipelines();
        PipelinePointer found = pipelinePointers.stream()
                .filter(pipelinePointer -> pipelinePointer.getId().equals(pipelinePointerId))
                .findFirst().orElseThrow(() -> new PipelineManagementException(PIPELINE_NOT_EXISTS));
        fieldSetConsumer.accept(found);
        save();
        return found;
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
    public Project createNewProject(AddProjectRequest request) {
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
        projectGroup.getProjects().add(project);
        logit(WORKSPACE_EVENT, "New project named: '" +
                    project.getName() + "', with id: '" + project.getId() + "', assigned to projectGroup: '" + projectGroup.getId() + "'");
        save();
        return project;
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
    public Project deleteProject(Long projectGroupId, Long projectId, boolean isHardRemoval) {
        // check if user not trying default secured project
        if (projectId.equals(defaultProjectId())) {
            logit(WORKSPACE_EVENT, "Cannot remove secured project");
            return null;
        }
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
            logit(WORKSPACE_EVENT, "Removed in a soft way project with id: '" + projectId + "'," +
                    "All pipelines pointers were moved to default 'other' project");
        } else {
            logit(WORKSPACE_EVENT, "Removed in a hard way project with id: '" + projectId + "'. It was permanently removed.");
        }
        projectGroup.getProjects().remove(projectToRemoval);
        save();
        return projectToRemoval;
    }

    private void movePipelinePointers(List<PipelinePointer> pipelinePointers, Project targetProject) {
        pipelinePointers.forEach(pipelinePointer -> pipelinePointer.setProjectId(targetProject.getId()));
        targetProject.getPipelines().addAll(pipelinePointers);
    }

    @Override
    public Project renameProject(Long projectId, String projectName) {
        Project project = findProject(projectId);
        final String oldName = project.getName();
        project.setName(projectName);
        project.setLastModifiedDate(new Date());
        save();
        logit(WORKSPACE_EVENT, "Project was renamed from '" + oldName + "', to: '" + projectName + "'");
        return project;
    }

    @Override
    public Project changeProjectTag(Long projectId, String projectTag) {
        Project project = findProject(projectId);
        final String oldTag = project.getTag();
        project.setTag(projectTag);
        project.setLastModifiedDate(new Date());
        save();
        logit(WORKSPACE_EVENT, "Project has changed tag from '" + oldTag + "', to: '" + projectTag + "'");
        return project;
    }

    @Override
    public Project changeProjectDescription(Long projectId, String projectDescription) {
        Project project = findProject(projectId);
        final String oldDescription = project.getDescription();
        project.setDescription(projectDescription);
        project.setLastModifiedDate(new Date());
        save();
        logit(WORKSPACE_EVENT, "Project has changed description from '" + oldDescription + "', to: '" + projectDescription + "'");
        return project;
    }

    static void refreshFileContext() {
        ProjectManager.projectManager = null;
        ProjectManager.getInstance();
    }

    @Override
    public ProjectGroup createNewProjectGroup(AddProjectGroupRequest request) {
        if (isProjectGroupExists(request.getName())) {
            logit(WORKSPACE_EVENT, "Cannot create project group because one with name: '" + request.getName() + "' just exists");
            throw new PipelineManagementException(PROJECT_GROUP_EXISTS);
        }
        ProjectGroup project = ProjectGroup.builder()
                .id(nextProjectGroupId(projectsFile))
                .cratedDate(new Date())
                .name(request.getName())
                .tag(request.getTag())
                .description(request.getDescription())
                .projects(new ArrayList<>(0))
                .build();
        projectsFile.getProjectGroups().add(project);
        save();
        return project;
    }

    private boolean isProjectGroupExists(String projectGroupName) {
        return projectsFile.getProjectGroups().stream()
                .anyMatch(projectGroup -> projectGroup.getName().equals(projectGroupName));
    }

    @Override
    public ProjectGroup deleteProjectGroup(Long projectGroupId, boolean isHardRemoval) {
        if (projectGroupId.equals(defaultProjectGroupId())) {
            logit(WORKSPACE_EVENT, "Cannot remove secured project group with id: '" + projectGroupId + "'");
            throw new PipelineManagementException(REMOVAL_DENIED);
        }
        ProjectGroup projectGroupToRemoval = projectsFile.getProjectGroups().stream()
                .filter(group -> group.getId().equals(projectGroupId))
                .findFirst()
                .orElseThrow(() -> new PipelineManagementException(PROJECT_GROUP_NOT_EXISTS));
        if (!isHardRemoval) {
            Long defaultProjectGroupId = defaultProjectGroupId();
            ProjectGroup defaultProjectGroup = projectsFile.getProjectGroups().stream()
                    .filter(group -> group.getId().equals(defaultProjectGroupId))
                    .findFirst()
                    .orElseThrow(() -> new PipelineManagementException(PROJECT_GROUP_NOT_EXISTS));
            List<Project> projects = projectGroupToRemoval.getProjects();
            defaultProjectGroup.getProjects().addAll(projects);
            logit(WORKSPACE_EVENT, "Removed in a soft way project group with id: '" + projectGroupId + "'," +
                    "All projects with their pipeline pointer were moved to default 'other' project group");
        } else {
            logit(WORKSPACE_EVENT, "Removed in a hard way project group with id: '" + projectGroupId + "'. It was permanently removed.");
        }
        projectsFile.getProjectGroups().remove(projectGroupToRemoval);
        save();
        return projectGroupToRemoval;
    }

    @Override
    public ProjectGroup renameProjectGroup(Long projectGroupId, String projectGroupName) {
        ProjectGroup projectGroup = projectsFile.getProjectGroups().stream()
                .filter(group -> group.getId().equals(projectGroupId))
                .findFirst()
                .orElseThrow(() -> new PipelineManagementException(PROJECT_GROUP_NOT_EXISTS));
        final String oldName = projectGroup.getName();
        projectGroup.setName(projectGroupName);
        projectGroup.setLastModifiedDate(new Date());
        save();
        logit(WORKSPACE_EVENT, "Project group was renamed from '" + oldName + "', to: '" + projectGroupName + "'");
        return projectGroup;
    }

    @Override
    public ProjectGroup changeTag(Long projectGroupId, String projectGroupTag) {
        ProjectGroup projectGroup = projectsFile.getProjectGroups().stream()
                .filter(group -> group.getId().equals(projectGroupId))
                .findFirst()
                .orElseThrow(() -> new PipelineManagementException(PROJECT_GROUP_NOT_EXISTS));
        final String oldTag = projectGroup.getTag();
        projectGroup.setTag(projectGroupTag);
        projectGroup.setLastModifiedDate(new Date());
        save();
        logit(WORKSPACE_EVENT, "Project group's tag was changed from '" + oldTag + "', to: '" + projectGroupTag + "'");
        return projectGroup;
    }

    @Override
    public ProjectGroup changeDescription(Long projectGroupId, String projectGroupDescription) {
        ProjectGroup projectGroup = projectsFile.getProjectGroups().stream()
                .filter(group -> group.getId().equals(projectGroupId))
                .findFirst()
                .orElseThrow(() -> new PipelineManagementException(PROJECT_GROUP_NOT_EXISTS));
        final String oldDescription = projectGroup.getDescription();
        projectGroup.setDescription(projectGroupDescription);
        projectGroup.setLastModifiedDate(new Date());
        save();
        logit(WORKSPACE_EVENT, "Project group's description was changed from '" + oldDescription + "', to: '" + projectGroupDescription + "'");
        return projectGroup;
    }

    public static void destroyInstance() {
        projectManager = null;
    }
}
