package io.easeci.core.engine.easefile.parser.parts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.easeci.core.engine.easefile.parser.analyse.SyntaxError;
import io.easeci.core.engine.easefile.parser.dto.StageDto;
import io.easeci.core.engine.pipeline.Stage;
import io.easeci.core.engine.pipeline.Step;
import io.easeci.core.workspace.vars.Variable;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.easeci.core.engine.easefile.parser.parts.Utils.findLineStartingWith;
import static io.easeci.core.engine.easefile.parser.parts.Utils.propertyToList;
import static java.util.Objects.nonNull;

@Slf4j
public class StageProcessor implements PipelinePartProcessor<List<Stage>> {

    public static final String PARSING_COMMAND_ERROR_TITLE = "Cannot parse step command and retrieve directive name and invocation body";

    private final ObjectMapper objectMapper;

    public StageProcessor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Tuple2<Optional<List<Stage>>, List<SyntaxError>> process(Supplier<List<Line>> easefilePartSupplier) {
        final List<Line> lines = easefilePartSupplier.get();
        final List<SyntaxError> syntaxErrors = new ArrayList<>(0);

        if (lines.isEmpty()) {
            return Tuple.of(Optional.of(Collections.emptyList()), syntaxErrors);
        }
        final String joined = propertyToList(lines);
        try {
            List<StageDto> stageDtos = objectMapper.readValue(joined, new TypeReference<List<StageDto>>() {});
            List<Stage> stages = stageDtos.stream()
                                          .map(stageDto -> from(stageDto, lines, syntaxErrors, stageDtos.indexOf(stageDto)))
                                          .collect(Collectors.toList());
            return Tuple.of(Optional.of(stages), syntaxErrors);
        } catch (JsonProcessingException e) {
            final int lineNr = e.getLocation().getLineNr() + 1;
            syntaxErrors.add(SyntaxError.builder()
                        .lineNumber(lineNr)
                        .title(PARSING_COMMAND_ERROR_TITLE)
                        .info(e.getMessage())
                        .build());
            e.printStackTrace();
            return Tuple.of(Optional.of(Collections.emptyList()), syntaxErrors);
        }
    }

    private Stage from(StageDto stageDto, List<Line> lines, List<SyntaxError> syntaxErrors, int order) {
        List<Variable> variables = null;
        if (nonNull(stageDto.getVariables())) {
            Tuple2<List<Variable>, List<SyntaxError>> tupleResult = VariableProcessor.mapVariables(stageDto.getVariables(), lines);
            variables = tupleResult._1;
            syntaxErrors.addAll(tupleResult._2);
        }
        return Stage.builder()
                .name(stageDto.getStageName())
                .order(order)
                .steps(stageDto.getSteps().stream().map(cmd -> {
                    final String[] split = cmd.split("\\s", 2);
                    if (split.length < 2) {
                        log.error(PARSING_COMMAND_ERROR_TITLE);
                        syntaxErrors.add(SyntaxError.builder()
                                .lineNumber(findLineStartingWith(lines, cmd)
                                        .map(line -> line.getLineNumber() + 1)
                                        .orElse(1))
                                .title(PARSING_COMMAND_ERROR_TITLE)
                                .info("Declaration of step is not correct. It must consists of $ sign, directive name and other parameters")
                                .build());
                    }
                    return new Step(stageDto.getSteps().indexOf(cmd), split[0].trim(), split[1].trim());
                }).collect(Collectors.toList()))
                .variables(variables)
                .build();
    }
}
