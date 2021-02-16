package io.easeci.core.engine.easefile.parser.parts;

import io.easeci.core.engine.easefile.parser.analyse.SyntaxError;
import io.easeci.core.engine.pipeline.Key;
import io.vavr.Tuple2;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class KeyProcessor implements PipelinePartProcessor<Key> {
    @Override
    public Tuple2<Optional<Key>, List<SyntaxError>> process(Supplier<List<Line>> easefilePartSupplier) {
        return null;
    }
}
