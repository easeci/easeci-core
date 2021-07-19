package io.easeci.core.engine.runtime;

/**
 * Functional interface that has only one responsibility,
 * this only checks is context of pipeline is alive or not.
 * @author Karol Meksuła
 * 2021-07-19
 * */
public interface PipelineContextLivenessProbe {

    /**
     * @param clt - Context Life Time (in seconds)
     *            Describes how much time context will live without logging.
     *            Some kind of liveliness probe
     * */
    boolean isMaximumIdleTimePassed(long clt);
}
