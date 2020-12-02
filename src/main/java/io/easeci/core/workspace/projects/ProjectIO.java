package io.easeci.core.workspace.projects;

public interface ProjectIO {

    ProjectsFile createNewProject();

    ProjectsFile deleteProject();

    ProjectsFile renameProject();

    ProjectsFile changeTag();

    ProjectsFile changeDescription();
}
