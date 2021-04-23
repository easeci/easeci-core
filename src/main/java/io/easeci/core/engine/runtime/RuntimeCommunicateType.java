package io.easeci.core.engine.runtime;

/**
 * In runtime of Pipeline there could be thrown exceptions.
 * These exception could be critical errors or simple warnings.
 * @author Karol Meksuła
 * 2021-04-24
 * */
public enum RuntimeCommunicateType {
    /**
     * Use this enum constant if you want to mark exception as critical error.
     * It will cause that pipeline workflow will be interrupted and error will
     * be notified in log of pipeline run.
     * */
    ERROR,

    /**
     * Use this enum constant if you want to inform user about potential dangerous in code etc.
     * This warning don't stop execution of pipeline flow.
     * */
    WARNING
}
