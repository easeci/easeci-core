package io.easeci.core.engine.easefile.parser;

import io.easeci.core.engine.EngineStatus;
import io.easeci.core.engine.pipeline.Pipeline;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class EasefileParseResult {
    private Pipeline pipeline;
    private List<EngineStatus> errors;

    public void putError(EngineStatus engineError) {
        if (errors == null) {
            this.errors = new ArrayList<>(0);
        }
        this.errors.add(engineError);
    }

}
