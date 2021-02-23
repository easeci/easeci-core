package io.easeci.core.engine.pipeline;

import lombok.Data;

import java.util.List;

@Data
public class ExecutorConfiguration {
    private ExecutingStrategy executingStrategy;
    private List<Executor> predefinedExecutors;
}
