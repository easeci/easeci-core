package io.easeci.core.engine.easefile.parser.parts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.easeci.core.engine.easefile.parser.analyse.SyntaxError;
import io.easeci.core.workspace.vars.Variable;
import io.easeci.extension.command.VariableType;
import io.vavr.Tuple;
import io.vavr.Tuple2;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.easeci.core.engine.easefile.parser.parts.Utils.findLineStartingWith;
import static io.easeci.core.engine.easefile.parser.parts.Utils.propertyToList;

public class VariableProcessor implements PipelinePartProcessor<List<Variable>> {

    public static final String VARIABLE_TYPE_NOT_RECOGNIZE_TITLE = "Cannot infer type of variable";
    public static final String VARIABLE_SYNTAX_ERROR_TITLE = "Syntax error occurred";

    private final ObjectMapper objectMapper;

    public VariableProcessor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Tuple2<Optional<List<Variable>>, List<SyntaxError>> process(Supplier<List<Line>> easefilePartSupplier) {
        final List<Line> lines = easefilePartSupplier.get();
        final List<SyntaxError> syntaxErrors = new ArrayList<>(0);

        if (lines.isEmpty() || lines.size() == 1) {
            return Tuple.of(Optional.of(Collections.emptyList()), syntaxErrors);
        }
        final String joined = propertyToList(lines.subList(1, lines.size()));
        try {
            final Map<String, Object> variables = objectMapper.readValue(joined, new TypeReference<Map<String, Object>>() {});
            Tuple2<List<Variable>, List<SyntaxError>> tupleResult = mapVariables(variables, lines);
            syntaxErrors.addAll(tupleResult._2);
            return Tuple.of(Optional.of(tupleResult._1), syntaxErrors);
        } catch (JsonProcessingException e) {
            final int lineNr = e.getLocation().getLineNr() + 1;
            syntaxErrors.add(SyntaxError.builder()
                        .lineNumber(lineNr)
                        .title(VARIABLE_SYNTAX_ERROR_TITLE)
                        .info(e.getMessage())
                        .build());
            e.printStackTrace();
            return Tuple.of(Optional.empty(), syntaxErrors);
        }
    }

    static Tuple2<List<Variable>, List<SyntaxError>> mapVariables(Map<String, Object> variables, List<Line> lines) {
        List<SyntaxError> syntaxErrors = new ArrayList<>();
        return Tuple.of(variables.entrySet()
                .stream()
                .map(entry -> {
                    final Object value = entry.getValue();
                    if (value instanceof String) {
                        return Variable.of(VariableType.STRING, entry.getKey(), value);
                    }
                    if (value instanceof Number) {
                        return Variable.of(VariableType.NUMBER, entry.getKey(), value);
                    }
                    if (value instanceof List) {
                        return Variable.of(VariableType.LIST, entry.getKey(), value);
                    }
                    if (value instanceof Map) {
                        return Variable.of(VariableType.DICTIONARY, entry.getKey(), value);
                    }
                    syntaxErrors.add(SyntaxError.builder()
                            .lineNumber(findLineStartingWith(lines, entry.getKey())
                                    .map(line -> line.getLineNumber() + 1)
                                    .orElse(1))
                            .title(VARIABLE_TYPE_NOT_RECOGNIZE_TITLE)
                            .info("Type of variable is not recognized. Please remove this variable or fix it to correct format")
                            .build());
                    return null;
                }).filter(Objects::nonNull)
                .collect(Collectors.toList()), syntaxErrors);
    }
}
