package io.easeci.core.engine.easefile.parser.parts;

import io.easeci.core.engine.easefile.parser.analyse.SyntaxError;
import io.vavr.Tuple2;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class ScriptFileProcessor implements PipelinePartProcessor<byte[]> {
    @Override
    public Tuple2<Optional<byte[]>, List<SyntaxError>> process(Supplier<List<Line>> easefilePartSupplier) {
        return null;
    }
}
