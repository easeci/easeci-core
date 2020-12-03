package io.easeci.core.workspace.projects;

import io.easeci.core.engine.pipeline.Pipeline;

/**
 * Main interface to deal with Pipeline Pointers.
 * What is Pipeline Pointers? This is POJO object that represents 'alias'
 * or something in this taste that links to real pipeline created and ready to use.
 * @author Karol Meksuła
 * 2020-11-22
 * */
public interface PipelinePointerIO {

    /**
     * After Easefile parsing and after pipeline creation,
     * we must create PipelinePointer that will be assigned to
     * some project in projects-structure.json file in workspace.
     * It is required for navigation, managing and store all info in one place.
     * @param pipelineMeta is internal static call in Pipeline.class that holds all
     *                     most helpful and important information about Pipeline in system.
     * @return boolean value. If it is true - pipeline was created correctly.
     * @throws PipelineManagementException that inform us about validation result etc.
     *                                     In exception we have error code included, so
     *                                     we know what was wrong in the method flow and why pipeline was not created
     * */
    boolean createNewPipelinePointer(Pipeline.Metadata pipelineMeta) throws PipelineManagementException;

    /**
     * Simple delete method of PipelinePointer just existing in projects-structure.json
     * @param projectId is numeric project's id
     * @param pipelinePointerId is numeric pipelinePointer's id (don't confuse with pipelineId)
     * @return boolean value. If it is true - pipeline was deleted correctly.
     * @throws PipelineManagementException that inform us about validation result etc.
     *                                     In exception we have error code included, so
     *                                     we know what was wrong in the method flow and why pipeline was not removed
     * */
    boolean deletePipelinePointer(Long projectId, Long pipelinePointerId) throws PipelineManagementException;

    /**
     * Rename PipelinePointer just existing in projects-structure.json
     * @param projectId is numeric project's id
     * @param pipelinePointerId is numeric pipelinePointer's id (don't confuse with pipelineId)
     * @param pipelinePointerName is new name of pipeline pointer
     * @return boolean value. If it is true - pipeline was renamed correctly.
     * */
    boolean renamePipelinePointer(Long projectId, Long pipelinePointerId, String pipelinePointerName);

    /**
     * Rename PipelinePointer's tag just existing in projects-structure.json
     * @param projectId is numeric project's id
     * @param pipelinePointerId is numeric pipelinePointer's id (don't confuse with pipelineId)
     * @param tagName is new tag of pipeline pointer
     * @return boolean value. If it is true - pipeline was renamed correctly.
     * @throws PipelineManagementException that inform us about validation result etc.
     *                                     In exception we have error code included, so
     *                                     we know what was wrong in the method flow and why pipeline's tag was not renamed
     * */
    boolean changeTag(Long projectId, Long pipelinePointerId, String tagName);

    ProjectsFile changeDescription();
}
