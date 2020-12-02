package io.easeci.core.workspace.projects;

public interface ProjectGroupIO {

    ProjectsFile createNewProjectGroup();

    ProjectsFile deleteProjectGroup();

    ProjectsFile renameProjectGroup();

    ProjectsFile changeTag();

    ProjectsFile changeDescription();
}
