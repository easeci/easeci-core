package io.easeci.core.engine.easefile.parser.parts;

import io.easeci.BaseWorkspaceContextTest;
import io.easeci.core.engine.easefile.parser.analyse.SyntaxError;
import io.easeci.core.engine.pipeline.Executor;
import io.vavr.Tuple2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static io.easeci.core.engine.easefile.parser.Utils.*;
import static org.junit.jupiter.api.Assertions.*;

class ExecutorProcessorTest extends BaseWorkspaceContextTest {

    @Test
    @DisplayName("Should correctly parse executor: part of easefile when it is one liner")
    void parseOnelineSuccessTest() throws PipelinePartCriticalError {
        ExecutorProcessor executorProcessor = new ExecutorProcessor();

        EasefileExtractor easefileExtractor = new MainEasefileExtractor();
        String content = readValidTestEasefile();
        easefileExtractor.split(content);

        ExecutorExtractor executorExtractor = (ExecutorExtractor) easefileExtractor;
        List<Line> lines = executorExtractor.fetchCrudeExecutor();

        Tuple2<Optional<List<Executor>>, List<SyntaxError>> processed = executorProcessor.process(() -> lines);

        assertAll(() -> assertTrue(processed._1.isPresent()),
                  () -> assertEquals(1, processed._1.get().size()),
                  () -> assertTrue(processed._2.isEmpty()));
    }

    @Test
    @DisplayName("Should throw when executor is not specified in Easefile")
    void parseEmptyFailedTest() throws PipelinePartCriticalError {
        EasefileExtractor easefileExtractor = new MainEasefileExtractor();
        String content = readEmptyExecutorTestEasefile();

        assertThrows(PipelinePartCriticalError.class, () -> easefileExtractor.split(content));
    }

    @Test
    @DisplayName("Should correctly read data when there are multi executors configuration")
    void parseMultiExecutorSuccessTest() throws PipelinePartCriticalError {
        ExecutorProcessor executorProcessor = new ExecutorProcessor();

        EasefileExtractor easefileExtractor = new MainEasefileExtractor();
        String content = readMultiExecutorTestEasefile();
        easefileExtractor.split(content);

        ExecutorExtractor executorExtractor = (ExecutorExtractor) easefileExtractor;
        List<Line> lines = executorExtractor.fetchCrudeExecutor();

        Tuple2<Optional<List<Executor>>, List<SyntaxError>> processed = executorProcessor.process(() -> lines);

        assertAll(() -> assertTrue(processed._2.isEmpty()),
                  () -> assertTrue(processed._1.isPresent()),
                  () -> assertEquals(2, processed._1.get().size()));
    }
}