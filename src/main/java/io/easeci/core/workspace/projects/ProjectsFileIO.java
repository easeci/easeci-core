package io.easeci.core.workspace.projects;

import java.io.IOException;

public interface ProjectsFileIO {

    ProjectsFile projectFileState();

    ProjectsFile load() throws IOException;

    ProjectsFile reload();

    ProjectsFile save();
}
