package io.easeci.core.engine.easefile.parser.parts;

import io.easeci.core.engine.easefile.parser.analyse.SyntaxError;
import io.vavr.Tuple2;

import java.util.List;
import java.util.Optional;

public interface PipelinePartProcessor<T> {

    Tuple2<Optional<T>, List<SyntaxError>> process();
}
