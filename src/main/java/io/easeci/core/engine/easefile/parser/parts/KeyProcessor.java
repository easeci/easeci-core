package io.easeci.core.engine.easefile.parser.parts;

import io.easeci.core.engine.easefile.parser.analyse.SyntaxError;
import io.easeci.core.engine.pipeline.Key;
import io.vavr.Tuple;
import io.vavr.Tuple2;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class KeyProcessor implements PipelinePartProcessor<Key> {

    @Override
    public Tuple2<Optional<Key>, List<SyntaxError>> process(Supplier<List<Line>> easefilePartSupplier) {
        List<Line> lines = easefilePartSupplier.get();
        Line line = lines.get(0);
        final String KEY_AS_STRING = line.getContent().trim().substring(0, line.getContent().length() - 1).toUpperCase();
        return Tuple.of(Optional.ofNullable(Key.of(Key.KeyType.valueOf(KEY_AS_STRING))), Collections.emptyList());
    }
}
