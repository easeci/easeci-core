package io.easeci.core.workspace.projects;

import io.easeci.core.workspace.projects.dto.AddProjectGroupRequest;

/**
 * Main interface to deal with Project Group that is wrapper for Projects.
 * @author Karol Meksuła
 * 2020-11-22
 * */
public interface ProjectGroupIO {

    /**
     * As it was mentioned before, ProjectGroup is a POJO wrapper objects
     * that holds collection of Projects.
     * @param addProjectGroupRequest provides information about project group attributes like name, tag, description etc.
     *                               Notice that name of ProjectGroup must be unique.
     * @return projectGroup is just added object.
     * @throws PipelineManagementException that inform us about validation result etc.
     *                                     In exception we have error code included, so
     *                                     we know what was wrong in the method flow and why projectGroup was not created
     * */
    ProjectGroup createNewProjectGroup(AddProjectGroupRequest addProjectGroupRequest);

    ProjectGroup deleteProjectGroup();

    ProjectGroup renameProjectGroup();

    ProjectGroup changeTag();

    ProjectGroup changeDescription();
}
