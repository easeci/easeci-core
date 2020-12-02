package io.easeci.core.engine;

import io.easeci.core.node.NodeUtils;

/**
 * Error statuses for action from EaseCI Engine.
 * One place, unified to handling errors.
 *
 * How to read this?
 *  - position 0: indicates is it was failure(F) or success(S)
 *  - position 1: is delimiter '_'
 *  - position 2: indicates module which status is concerns
 *                modules:
 *                P - pipeline module
 *                E - easefile module
 *  - position 3: indicates action
 *                actions:
 *                P - parsing
 *                A - adding
 *                D - deleting
 *  - position 4: is delimiter '_'
 *  - position 5-8: code ordinal number
 * @author Karol Meksuła
 * 2020-11-23
 * */
public enum EngineStatus {
    F_PP_0001("Pipeline was created but something went wrong while adding PipelinePointer to projects-structure.json file");

    public String className;           // <- indicates class where error could be occurred
    public long codeLine;              // <- indicates line of code where error could be occurred
    private String applicationVersion; // <- inform what is EaseCI version
    private String errorCode;
    private String message;

    EngineStatus(String message) {
        this.applicationVersion = NodeUtils.version();
        this.errorCode = this.name();
        this.message = message;
    }
}
