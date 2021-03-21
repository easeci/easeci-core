package io.easeci.core.engine.easefile.parser.parts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.easeci.core.engine.easefile.parser.analyse.SyntaxError;
import io.easeci.core.engine.pipeline.EasefileObjectModel;
import io.vavr.Tuple;
import io.vavr.Tuple2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static io.easeci.core.engine.easefile.parser.parts.ExecutorProcessor.PARSING_LINE_ERROR_TITLE;
import static io.easeci.core.engine.easefile.parser.parts.Utils.propertyToList;

public class MetadataProcessor implements PipelinePartProcessor<EasefileObjectModel.Metadata> {

    private final ObjectMapper objectMapper;

    public MetadataProcessor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Tuple2<Optional<EasefileObjectModel.Metadata>, List<SyntaxError>> process(Supplier<List<Line>> easefilePartSupplier) {
        final List<Line> lines = easefilePartSupplier.get();
        final List<SyntaxError> syntaxErrors = new ArrayList<>(0);

        EasefileObjectModel.Metadata metadata = new EasefileObjectModel.Metadata();

        if (lines.isEmpty()) {
            return Tuple.of(Optional.of(metadata), syntaxErrors);
        }

        final String joined = propertyToList(lines);
        try {
            EasefileObjectModel.MetadataInput metadataInput = objectMapper.readValue(joined, EasefileObjectModel.MetadataInput.class);
            metadata = metadata.fromInput(metadataInput);
        } catch (JsonProcessingException e) {
            final int lineNr = e.getLocation().getLineNr() + 1;
            syntaxErrors.add(SyntaxError.builder()
                        .lineNumber(lineNr)
                        .title(PARSING_LINE_ERROR_TITLE)
                        .info(e.getMessage())
                        .build());
            e.printStackTrace();
            return Tuple.of(Optional.of(metadata), syntaxErrors);
        }
        return Tuple.of(Optional.of(metadata), syntaxErrors);
    }
}
