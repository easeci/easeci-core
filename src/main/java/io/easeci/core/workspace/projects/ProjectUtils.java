package io.easeci.core.workspace.projects;

public class ProjectUtils {

    static long nextPipelinePointerId(ProjectsFile projectsFile) {
        return projectsFile.getProjectGroups()
                .stream()
                .flatMap(projectGroup -> projectGroup.getProjects().stream())
                .flatMap(project -> project.getPipelines().stream())
                .count();
    }

    static long nextProjectId(ProjectsFile projectsFile) {
        return projectsFile.getProjectGroups()
                .stream()
                .flatMap(projectGroup -> projectGroup.getProjects().stream())
                .count();
    }
}
