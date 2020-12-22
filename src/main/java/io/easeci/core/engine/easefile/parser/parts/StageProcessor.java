package io.easeci.core.engine.easefile.parser.parts;

import io.easeci.core.engine.easefile.parser.analyse.SyntaxError;
import io.easeci.core.engine.pipeline.Stage;
import io.vavr.Tuple2;

import java.util.List;
import java.util.Optional;

public class StageProcessor implements PipelinePartProcessor<List<Stage>> {
    @Override
    public Tuple2<Optional<List<Stage>>, List<SyntaxError>> process() {
        return null;
    }
}
