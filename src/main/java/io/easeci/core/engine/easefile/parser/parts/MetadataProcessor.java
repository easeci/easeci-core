package io.easeci.core.engine.easefile.parser.parts;

import io.easeci.core.engine.easefile.parser.analyse.SyntaxError;
import io.easeci.core.engine.pipeline.Pipeline;
import io.vavr.Tuple2;

import java.util.List;
import java.util.Optional;

public class MetadataProcessor implements PipelinePartProcessor<Pipeline.Metadata> {

    @Override
    public Tuple2<Optional<Pipeline.Metadata>, List<SyntaxError>> process() {
        return null;
    }
}
