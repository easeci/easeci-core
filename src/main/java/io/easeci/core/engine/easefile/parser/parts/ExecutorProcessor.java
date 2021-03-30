package io.easeci.core.engine.easefile.parser.parts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.easeci.core.engine.easefile.parser.analyse.SyntaxError;
import io.easeci.core.engine.pipeline.ExecutingStrategy;
import io.easeci.core.engine.pipeline.Executor;
import io.easeci.core.engine.pipeline.ExecutorConfiguration;
import io.easeci.core.node.NodesManager;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.Data;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.easeci.core.engine.easefile.parser.parts.Utils.propertyToList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class ExecutorProcessor implements PipelinePartProcessor<ExecutorConfiguration> {

    public static final String PARSING_LINE_ERROR_TITLE = "Error occurred while Easefile parsing process";
    public static final String PARSING_SECTION_ERROR_TITLE = "Cannot parse executor section";
    public static final String EXECUTOR_NOT_PRESENT_ERROR_TITLE = "At least one executor is required";

    private final ObjectMapper objectMapper;
    private final NodesManager nodesManager;

    public ExecutorProcessor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.nodesManager = new NodesManager();
    }

    @Override
    public Tuple2<Optional<ExecutorConfiguration>, List<SyntaxError>> process(Supplier<List<Line>> easefilePartSupplier) {
        final List<Line> lines = easefilePartSupplier.get();
        final List<SyntaxError> syntaxErrors = new ArrayList<>(0);
        final ExecutorConfiguration executorConfiguration = new ExecutorConfiguration();

        final String joined = propertyToList(lines.subList(1, lines.size()));
        ExecutorSection executorSection;
        try {
            executorSection = objectMapper.readValue(joined, ExecutorSection.class);
        } catch (JsonProcessingException e) {
            final int lineNr = e.getLocation().getLineNr() + 1;
            syntaxErrors.add(SyntaxError.builder()
                    .lineNumber(lineNr)
                    .title(PARSING_LINE_ERROR_TITLE)
                    .info(e.getMessage())
                    .build());
            e.printStackTrace();
            return Tuple.of(Optional.of(executorConfiguration), syntaxErrors);
        }
        if (isNull(executorSection)) {
            syntaxErrors.add(SyntaxError.builder()
                    .lineNumber(lines.get(0).getLineNumber())
                    .title(PARSING_SECTION_ERROR_TITLE)
                    .info("Executor section must be correctly defined in Easefile")
                    .build());
        } else {
            final ExecutingStrategy executingStrategy = ExecutingStrategy.fromString(executorSection.getStrategy());
            List<SyntaxError> errors = this.assembleConfiguration(executorConfiguration, executingStrategy, executorSection, lines.get(0));
            syntaxErrors.addAll(errors);
        }
        return Tuple.of(Optional.of(executorConfiguration), syntaxErrors);
    }

    private List<Executor> selectExecutorsByPriority(ExecutorSection executorSection) {
        // nodeUuids have the highest priority
        if (nonNull(executorSection.getNodeUuids()) && !executorSection.getNodeUuids().isEmpty()) {
            return executorSection.nodeUuids.stream()
                    .map(Executor::of)
                    .collect(Collectors.toList());
        }
        // names have lower priority - select only when there is no nodeUuids typed
        if (nonNull(executorSection.getNames()) && !executorSection.getNames().isEmpty()) {
            return executorSection.names.stream()
                    .map(nodesManager::resolveNodeByName)
                    .map(Executor::of)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private List<SyntaxError> assembleConfiguration(ExecutorConfiguration executorConfiguration, ExecutingStrategy executingStrategy,
                                              ExecutorSection executorSection, Line line) {
        if (ExecutingStrategy.MASTER.equals(executingStrategy)) {
            executorConfiguration.setExecutingStrategy(ExecutingStrategy.MASTER);
            executorConfiguration.setPredefinedExecutors(null);
        }
        if (ExecutingStrategy.AUTO.equals(executingStrategy)) {
            executorConfiguration.setExecutingStrategy(ExecutingStrategy.AUTO);
            executorConfiguration.setPredefinedExecutors(null);
        }
        if (ExecutingStrategy.ONE_OF.equals(executingStrategy)) {
            List<Executor> executors = selectExecutorsByPriority(executorSection);
            executorConfiguration.setExecutingStrategy(ExecutingStrategy.ONE_OF);
            if (executors.isEmpty()) {
                return List.of(SyntaxError.builder()
                        .lineNumber(line.getLineNumber())
                        .title(EXECUTOR_NOT_PRESENT_ERROR_TITLE)
                        .info("It is required to define at least one executor in your Easefile. " +
                                "You can define executingStrategy as AUTO, then system will choose automatically executor that is idle")
                        .build());
            }
            executorConfiguration.setPredefinedExecutors(executors);
        }
        if (ExecutingStrategy.EACH.equals(executingStrategy)) {
            List<Executor> executors = selectExecutorsByPriority(executorSection);
            executorConfiguration.setExecutingStrategy(ExecutingStrategy.EACH);
            if (executors.isEmpty()) {
                return List.of(SyntaxError.builder()
                        .lineNumber(line.getLineNumber())
                        .title(EXECUTOR_NOT_PRESENT_ERROR_TITLE)
                        .info("It is required to define at least one executor in your Easefile. " +
                                "You can define executingStrategy as AUTO, then system will choose automatically executor that is idle")
                        .build());
            }
            executorConfiguration.setPredefinedExecutors(executors);
        }
        return Collections.emptyList();
    }

    @Data
    private static class ExecutorSection {
        private String strategy;
        private List<String> names;
        private List<UUID> nodeUuids;
    }
}
