package io.easeci.core.engine.easefile.parser.parts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.easeci.core.engine.easefile.parser.analyse.SyntaxError;
import io.easeci.core.engine.pipeline.EasefileObjectModel;
import io.easeci.core.workspace.ProjectsValidator;
import io.easeci.core.workspace.projects.ProjectManager;
import io.vavr.Tuple;
import io.vavr.Tuple2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static io.easeci.core.engine.easefile.parser.parts.ExecutorProcessor.PARSING_LINE_ERROR_TITLE;
import static io.easeci.core.engine.easefile.parser.parts.Utils.findLineStartingWith;
import static io.easeci.core.engine.easefile.parser.parts.Utils.propertyToList;
import static java.util.Objects.nonNull;

public class MetadataProcessor implements PipelinePartProcessor<EasefileObjectModel.Metadata> {

    public static final String PROJECT_NOT_EXISTS_ERROR_TITLE = "Project with typed projectId not exists in EaseCI instance";

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

        final String joined = propertyToList(lines.subList(1, lines.size()));
        try {
            EasefileObjectModel.MetadataInput metadataInput = objectMapper.readValue(joined, EasefileObjectModel.MetadataInput.class);
            metadata = metadata.fromInput(metadataInput);
            if (nonNull(metadata.getProjectId())) {
                ProjectsValidator projectsValidator = ProjectManager.getInstance();
                boolean projectExists = projectsValidator.isProjectExists(metadata.getProjectId());
                if (!projectExists) {
                    syntaxErrors.add(SyntaxError.builder()
                            .lineNumber(findLineStartingWith(lines, "projectId")
                                    .map(line -> line.getLineNumber() + 1)
                                    .orElse(1))
                            .title(PROJECT_NOT_EXISTS_ERROR_TITLE)
                            .info("You need to type correct projectId of Project that exists in system or leave this property empty")
                            .build());
                }
            }
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
