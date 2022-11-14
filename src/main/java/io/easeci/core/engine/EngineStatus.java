package io.easeci.core.engine;

import io.easeci.core.node.connect.ClusterInformation;
import io.easeci.core.node.connect.ClusterInformationDefault;
import lombok.Getter;

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
@Getter
public enum EngineStatus {
    S_EP_0000("Pipeline was successfully created, file created, pointer in project-structure.json created"),
    F_PP_0001("Pipeline was created but something went wrong while adding PipelinePointer to projects-structure.json file"),
    F_EP_0002("Easefile parsing failed due to some syntax errors, any pipeline was not created"),
    F_EP_0003("Easefile parsing failed. Critical internal error occurred while collecting invocations of Easefile's parser. " +
            "Some directives may return null value or could not call directive correctly. " +
            "Another reason may be EasefileParser.class broken object");

    private String applicationVersion; // <- inform what is EaseCI version
    private String errorCode;
    private String message;

    private final ClusterInformation clusterInformation = new ClusterInformationDefault();

    EngineStatus(String message) {
        this.applicationVersion = clusterInformation.version();
        this.errorCode = this.name();
        this.message = message;
    }
}
