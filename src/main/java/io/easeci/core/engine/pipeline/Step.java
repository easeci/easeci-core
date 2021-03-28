package io.easeci.core.engine.pipeline;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Step {
    private final int order;
    private final String directiveName;
    private final String invocationBody;
}
