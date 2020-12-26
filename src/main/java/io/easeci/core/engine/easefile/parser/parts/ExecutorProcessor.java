package io.easeci.core.engine.easefile.parser.parts;

import io.easeci.core.engine.easefile.parser.analyse.SyntaxError;
import io.easeci.core.engine.pipeline.Executor;
import io.vavr.Tuple2;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class ExecutorProcessor implements PipelinePartProcessor<List<Executor>> {
    @Override
    public Tuple2<Optional<List<Executor>>, List<SyntaxError>> process(Supplier<String> easefilePartSupplier) {
        return null;
    }
}
