package io.easeci.core.engine.easefile.parser.parts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.easeci.BaseWorkspaceContextTest;
import io.easeci.core.engine.easefile.parser.analyse.SyntaxError;
import io.easeci.core.workspace.vars.Variable;
import io.vavr.Tuple2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static io.easeci.core.engine.easefile.parser.parts.Feeder.*;
import static io.easeci.core.engine.easefile.parser.parts.VariableProcessor.VARIABLE_SYNTAX_ERROR_TITLE;
import static org.junit.jupiter.api.Assertions.*;

class VariableProcessorTest  extends BaseWorkspaceContextTest {

    private static final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

    @Test
    @DisplayName("Should correctly parse simple example of variables declaration")
    void successTest() throws PipelinePartCriticalError {
        VariableProcessor variableProcessor = new VariableProcessor(objectMapper);

        Supplier<List<Line>> lines = provideCorrectVariables();

        Tuple2<Optional<List<Variable>>, List<SyntaxError>> result = variableProcessor.process(lines);

        final List<Variable> variables = result._1.get();
        final List<SyntaxError> syntaxErrors = result._2;

        assertAll(() -> assertTrue(syntaxErrors.isEmpty()),
                  () -> assertEquals(10, variables.size()));
    }

    @Test
    @DisplayName("Should correctly parse simple example of variables declaration when it is empty")
    void successEmptyTest() {
        VariableProcessor variableProcessor = new VariableProcessor(objectMapper);

        Supplier<List<Line>> lines = provideCorrectVariables2();

        Tuple2<Optional<List<Variable>>, List<SyntaxError>> result = variableProcessor.process(lines);

        final List<Variable> variables = result._1.get();
        final List<SyntaxError> syntaxErrors = result._2;

        assertAll(() -> assertTrue(syntaxErrors.isEmpty()),
                  () -> assertEquals(0, variables.size()));
    }

    @Test
    @DisplayName("Should correctly parse variables declaration with nested dictionary object")
    void successNestedObjectTest() {
        VariableProcessor variableProcessor = new VariableProcessor(objectMapper);

        Supplier<List<Line>> lines = provideCorrectVariables3();

        Tuple2<Optional<List<Variable>>, List<SyntaxError>> result = variableProcessor.process(lines);

        final List<Variable> variables = result._1.get();
        final Variable<Map<String, Object>> variable = variables.get(0);
        final List<SyntaxError> syntaxErrors = result._2;

        Object human = variable.getValue().get("human");
        List<String> friends = (List<String>) ((Map<String, Object>) human).get("_friends");

        assertAll(() -> assertTrue(syntaxErrors.isEmpty()),
                  () -> assertEquals(1, variables.size()),
                  () -> assertNotNull(variable),
                  () -> assertTrue(human instanceof Map),
                  () -> assertEquals(3, friends.size()));
    }

    @Test
    @DisplayName("Should failed parse but should correctly return error with clear communicate")
    void failureTest() {
        VariableProcessor variableProcessor = new VariableProcessor(objectMapper);

        Supplier<List<Line>> lines = provideCorrectVariables4();

        Tuple2<Optional<List<Variable>>, List<SyntaxError>> result = variableProcessor.process(lines);

        List<SyntaxError> expectedErrors = result._2;

        assertAll(() -> assertFalse(expectedErrors.isEmpty()),
                  () -> assertEquals(1, expectedErrors.size()),
                  () -> assertEquals(2, expectedErrors.get(0).getLineNumber()),
                  () -> assertEquals(VARIABLE_SYNTAX_ERROR_TITLE, expectedErrors.get(0).getTitle()));
    }
}