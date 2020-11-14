package io.easeci.core.workspace.projects;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Data
public class ProjectsFile implements Serializable {
    private List<ProjectGroup> projectGroups;

    public static ProjectsFile empty() {
        ProjectsFile projectsFile = new ProjectsFile();
        projectsFile.setProjectGroups(Collections.emptyList());
        return projectsFile;
    }
}
