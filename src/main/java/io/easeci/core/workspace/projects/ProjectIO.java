package io.easeci.core.workspace.projects;

import io.easeci.core.workspace.projects.dto.AddProjectRequest;

/**
 * Main interface to deal with Project that is wrapper for Pipeline Pointers.
 * @author Karol Meksuła
 * 2020-11-22
 * */
public interface ProjectIO {

    /**
     * As it was mentioned before, Project is a POJO wrapper objects
     * that holds collection of pipeline pointers.
     * @param request provides information about project attributes like name, tag, description etc.
     *                in request we can optionally specify project group where project will be assigned
     * @return boolean value. If it is true - project was created correctly.
     * @throws PipelineManagementException that inform us about validation result etc.
     *                                     In exception we have error code included, so
     *                                     we know what was wrong in the method flow and why project was not created
     * */
    boolean createNewProject(AddProjectRequest request);

    boolean deleteProject(Long projectGroupId, Long projectId, boolean isHardRemoval);

    boolean renameProject();

    boolean changeProjectTag();

    boolean changeProjectDescription();
}
