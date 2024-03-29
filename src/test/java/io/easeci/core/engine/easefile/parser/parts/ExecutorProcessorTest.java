package io.easeci.core.engine.easefile.parser.parts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.easeci.BaseWorkspaceContextTest;
import io.easeci.core.engine.easefile.parser.analyse.SyntaxError;
import io.easeci.core.engine.pipeline.ExecutingStrategy;
import io.easeci.core.engine.pipeline.Executor;
import io.easeci.core.engine.pipeline.ExecutorConfiguration;
import io.easeci.core.node.connect.ClusterNodesProvider;
import io.vavr.Tuple2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static io.easeci.core.engine.easefile.parser.Utils.*;
import static io.easeci.core.engine.easefile.parser.parts.ExecutorProcessor.PARSING_LINE_ERROR_TITLE;
import static io.easeci.core.engine.easefile.parser.parts.Feeder.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class ExecutorProcessorTest extends BaseWorkspaceContextTest {

    private static final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

    @Test
    @DisplayName("Should NOT throw when executor is not specified in Easefile")
    void parseEmptyFailedTest() {
        EasefileExtractor easefileExtractor = new MainEasefileExtractor();
        String content = readEmptyExecutorTestEasefile();

        assertDoesNotThrow(() -> easefileExtractor.split(content));
    }

    @Test
    @DisplayName("Should correctly parse simple content of executor part with AUTO strategy")
    void parseSuccessTest() {
        Supplier<List<Line>> linesSupplier = provideCorrectExecutor1();

        ExecutorProcessor executorProcessor = new ExecutorProcessor(objectMapper);
        Tuple2<Optional<ExecutorConfiguration>, List<SyntaxError>> processingResult = executorProcessor.process(linesSupplier);

        ExecutorConfiguration executorConfiguration = processingResult._1().get();
        assertAll(
                // predefined executors are null - we ignore these if executing strategy is AUTO
                    () -> assertNull(executorConfiguration.getPredefinedExecutors()),
                    () -> assertEquals(ExecutingStrategy.AUTO, executorConfiguration.getExecutingStrategy()));
    }

    @Test
    @DisplayName("Should correctly parse content of executor part - nodeUuids has higher priority than names")
    void parseOnlyFirstPropertySuccessTest() {
        Supplier<List<Line>> linesSupplier = provideCorrectExecutor8();

        ExecutorProcessor executorProcessor = new ExecutorProcessor(objectMapper);
        Tuple2<Optional<ExecutorConfiguration>, List<SyntaxError>> processingResult = executorProcessor.process(linesSupplier);

        ExecutorConfiguration executorConfiguration = processingResult._1().get();
        assertAll(
                    // in content of Easefile there are nodeUuids and names properties...
                    // in such case nodeUuids has higher priority that names
                    () -> assertEquals(3, executorConfiguration.getPredefinedExecutors().size()),
                    () -> assertEquals(ExecutingStrategy.EACH, executorConfiguration.getExecutingStrategy()));
    }

    @Test
    @DisplayName("Should correctly parse simple content of executor part with not specified strategy")
    void parseUndefinedSuccessTest() {
        Supplier<List<Line>> linesSupplier = provideCorrectExecutor2();

        ExecutorProcessor executorProcessor = new ExecutorProcessor(objectMapper);
        Tuple2<Optional<ExecutorConfiguration>, List<SyntaxError>> processingResult = executorProcessor.process(linesSupplier);

        ExecutorConfiguration executorConfiguration = processingResult._1().get();
        assertAll(
                // predefined executors are null - we ignore these if executing strategy is AUTO
                () -> assertNull(executorConfiguration.getPredefinedExecutors()),
                () -> assertEquals(ExecutingStrategy.AUTO, executorConfiguration.getExecutingStrategy()));
    }

    @Test
    @DisplayName("Should correct parse simple content of executor part with EACH strategy and 'names' typed")
    void parseEachSuccessTest() {
        Supplier<List<Line>> linesSupplier = provideCorrectExecutor3();
        ClusterNodesProvider clusterNodesProvider = Mockito.mock(ClusterNodesProvider.class);
        Mockito.when(clusterNodesProvider.findByNodeName(any())).thenReturn(Optional.of(UUID::randomUUID));

        ExecutorProcessor executorProcessor = new ExecutorProcessor(objectMapper, clusterNodesProvider);
        Tuple2<Optional<ExecutorConfiguration>, List<SyntaxError>> processingResult = executorProcessor.process(linesSupplier);

        ExecutorConfiguration executorConfiguration = processingResult._1().get();
        assertAll(
                () -> assertNotNull(executorConfiguration.getPredefinedExecutors()),
                () -> assertEquals(2, executorConfiguration.getPredefinedExecutors().size()),
                () -> assertEquals(ExecutingStrategy.EACH, executorConfiguration.getExecutingStrategy()));
    }

    @Test
    @DisplayName("Should correct parse simple content of executor part with EACH strategy and 'nodeUuids' typed")
    void parseEachNodeUuidsSuccessTest() {
        Supplier<List<Line>> linesSupplier = provideCorrectExecutor4();

        ExecutorProcessor executorProcessor = new ExecutorProcessor(objectMapper);
        Tuple2<Optional<ExecutorConfiguration>, List<SyntaxError>> processingResult = executorProcessor.process(linesSupplier);

        ExecutorConfiguration executorConfiguration = processingResult._1().get();
        assertAll(
                () -> assertEquals(3, executorConfiguration.getPredefinedExecutors().size()),
                () -> assertEquals(ExecutingStrategy.EACH, executorConfiguration.getExecutingStrategy()));
    }

    @Test
    @DisplayName("Should correct parse simple content of executor part with ONE_OF strategy and 'nodeUuids' typed")
    void parseOneOfNodeUuidsSuccessTest() {
        Supplier<List<Line>> linesSupplier = provideCorrectExecutor5();

        ExecutorProcessor executorProcessor = new ExecutorProcessor(objectMapper);
        Tuple2<Optional<ExecutorConfiguration>, List<SyntaxError>> processingResult = executorProcessor.process(linesSupplier);

        ExecutorConfiguration executorConfiguration = processingResult._1().get();
        assertAll(
                () -> assertEquals(3, executorConfiguration.getPredefinedExecutors().size()),
                () -> assertEquals(ExecutingStrategy.ONE_OF, executorConfiguration.getExecutingStrategy()));
    }

    @Test
    @DisplayName("Should failed when trying to parse simple content of executor part with ONE_OF strategy without typing 'nodeUuids' or 'names'")
    void parseFailureTest() {
        Supplier<List<Line>> linesSupplier = provideCorrectExecutor7();

        ExecutorProcessor executorProcessor = new ExecutorProcessor(objectMapper);
        Tuple2<Optional<ExecutorConfiguration>, List<SyntaxError>> processingResult = executorProcessor.process(linesSupplier);

        ExecutorConfiguration executorConfiguration = processingResult._1().get();
        assertAll(() -> assertEquals(1, processingResult._2.size()),
                  () -> assertNull(executorConfiguration.getPredefinedExecutors()),
                  () -> assertEquals(ExecutingStrategy.ONE_OF, executorConfiguration.getExecutingStrategy()));
    }

    @Test
    @DisplayName("Should failed when UUID in nodeUuids is not well formatted")
    void parseFailureUuidFormatTest() {
        Supplier<List<Line>> linesSupplier = provideCorrectExecutor9();

        ExecutorProcessor executorProcessor = new ExecutorProcessor(objectMapper);
        Tuple2<Optional<ExecutorConfiguration>, List<SyntaxError>> processingResult = executorProcessor.process(linesSupplier);

        SyntaxError expectedSyntaxError = processingResult._2.get(0);

        assertAll(() -> assertEquals(1, processingResult._2.size()),
                  () -> assertEquals(4, expectedSyntaxError.getLineNumber()),
                  () -> assertEquals(PARSING_LINE_ERROR_TITLE, expectedSyntaxError.getTitle()));
    }

    @Test
    @DisplayName("Should failed when trying to parse simple content of executor part with ONE_OF strategy and typing empty 'nodeUuids'")
    void parseFailureEmptyListTest() {
        Supplier<List<Line>> linesSupplier = provideCorrectExecutor10();

        ExecutorProcessor executorProcessor = new ExecutorProcessor(objectMapper);
        Tuple2<Optional<ExecutorConfiguration>, List<SyntaxError>> processingResult = executorProcessor.process(linesSupplier);

        SyntaxError expectedSyntaxError = processingResult._2.get(0);

        System.out.println(expectedSyntaxError);

        assertAll(() -> assertEquals(1, processingResult._2.size()),
                  () -> assertEquals("At least one executor is required", expectedSyntaxError.getTitle()));
    }
}