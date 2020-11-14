package io.easeci.core.workspace.projects;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.easeci.commons.DirUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelName.WORKSPACE_EVENT;
import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelPrefix.THREE;
import static io.easeci.core.log.ApplicationLevelLogFacade.logit;
import static io.easeci.core.workspace.LocationUtils.getProjectsStructureFileLocation;
import static io.easeci.core.workspace.LocationUtils.getWorkspaceLocation;

public class ProjectManager {
    public final static String PROJECTS_DIRECTORY = "/projects/";
    public final static String PROJECTS_FILE = PROJECTS_DIRECTORY + "projects-structure.json";
    private static ProjectManager projectManager;
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private ProjectsFile projectsFile;

    private ProjectManager() {
        logit(WORKSPACE_EVENT, "Initialization of projects place in workspace of: " + PROJECTS_FILE, THREE);
        this.initializeDirectory();
        this.initializeProjectsFile();
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
            ProjectsFile projectsFile = ProjectsFile.empty();
            String projectsFileAsString = OBJECT_MAPPER.writeValueAsString(projectsFile);
            Files.writeString(projectsStructureFile, projectsFileAsString);
        } catch (IOException e) {
            e.printStackTrace();
            logit(WORKSPACE_EVENT, "Exception was thrown when trying to create file: " + PROJECTS_FILE);
            return projectsStructureFile;
        }
        return projectsStructureFile;
    }

    private ProjectsFile load() throws IOException {
        Path projectsStructureFileLocation = getProjectsStructureFileLocation();
        return OBJECT_MAPPER.readValue(projectsStructureFileLocation.toFile(), ProjectsFile.class);
    }
}
