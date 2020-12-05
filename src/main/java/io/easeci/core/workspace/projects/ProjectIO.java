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

    /**
     * This method removes project. You can remove all project with one exception:
     * ProjectGroup and Project with id: 0 is secured.
     * @param projectGroupId is id of project group
     * @param projectId is id of project in project group identified with projectGroupId
     * @return boolean value. If it is true - project was removed correctly.
     * @throws PipelineManagementException that inform us about validation result etc.
     *                                     In exception we have error code included, so
     *                                     we know what was wrong in the method flow and why project was not removed.
     *                                     For example it is throwing when project not exists.
     * */
    boolean deleteProject(Long projectGroupId, Long projectId, boolean isHardRemoval);

    /**
     * This method renames project.
     * @param projectId is id of project in project group identified with projectGroupId
     * @param projectName is new name of project
     * @return boolean value. If it is true - project was renamed correctly.
     * @throws PipelineManagementException that inform us about validation result etc.
     *                                     In exception we have error code included, so
     *                                     we know what was wrong in the method flow and why project was not renamed.
     *                                     For example it is throwing when project not exists.
     * */
    boolean renameProject(Long projectId, String projectName);

    /**
     * This method edits tag of project.
     * @param projectId is id of project in project group identified with projectGroupId
     * @param projectTag is new tag of project
     * @return boolean value. If it is true - project's tag was changed correctly.
     * @throws PipelineManagementException that inform us about validation result etc.
     *                                     In exception we have error code included, so
     *                                     we know what was wrong in the method flow and why project's tag was not changed.
     *                                     For example it is throwing when project not exists.
     * */
    boolean changeProjectTag(Long projectId, String projectTag);

    /**
     * This method edits description of project.
     * @param projectId is id of project in project group identified with projectGroupId
     * @param projectDescription is new description of project
     * @return boolean value. If it is true - project's description was changed correctly.
     * @throws PipelineManagementException that inform us about validation result etc.
     *                                     In exception we have error code included, so
     *                                     we know what was wrong in the method flow and why project's description was not changed.
     *                                     For example it is throwing when project not exists.
     * */
    boolean changeProjectDescription(Long projectId, String projectDescription);
}
