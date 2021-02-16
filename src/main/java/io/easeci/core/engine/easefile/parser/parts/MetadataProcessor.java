package io.easeci.core.engine.easefile.parser.parts;

import io.easeci.core.engine.easefile.parser.analyse.SyntaxError;
import io.easeci.core.engine.pipeline.EasefileObjectModel;
import io.vavr.Tuple2;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class MetadataProcessor implements PipelinePartProcessor<EasefileObjectModel.Metadata> {

    @Override
    public Tuple2<Optional<EasefileObjectModel.Metadata>, List<SyntaxError>> process(Supplier<List<Line>> easefilePartSupplier) {
        return null;
    }
}
