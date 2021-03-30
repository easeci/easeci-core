package io.easeci.core.engine.easefile.parser.parts;

import io.easeci.BaseWorkspaceContextTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.easeci.core.engine.easefile.parser.Utils.readFinalCorrectEasefile;
import static org.junit.jupiter.api.Assertions.*;

class MainEasefileExtractorTest extends BaseWorkspaceContextTest {

    @Test
    @DisplayName("Should throw exception when trying to fetch crude executor without before invoke split() method")
    void fetchCrudeExceptionTest() {
        EasefileExtractor easefileExtractor = new MainEasefileExtractor();

        ExecutorExtractor executorExtractor = (ExecutorExtractor) easefileExtractor;

        assertThrows(PipelinePartError.class, executorExtractor::fetchCrudeExecutor);
    }

    @Test
    @DisplayName("Should correctly fetch crude parts of Easefile")
    void fetchCrudeSuccessTest() throws PipelinePartCriticalError {
        EasefileExtractor easefileExtractor = new MainEasefileExtractor();
        KeyExtractor keyExtractor = (KeyExtractor) easefileExtractor;
        ExecutorExtractor executorExtractor = (ExecutorExtractor) easefileExtractor;
        VariableExtractor variableExtractor = (VariableExtractor) easefileExtractor;
        StageExtractor stageExtractor = (StageExtractor) easefileExtractor;

        String content = readFinalCorrectEasefile();
        easefileExtractor.split(content);

        List<Line> keyPart = keyExtractor.fetchCrudeKey();
        List<Line> executorPart = executorExtractor.fetchCrudeExecutor();
        List<Line> variablesPart = variableExtractor.fetchCrudeVariable();
        List<Line> stagesPart = stageExtractor.fetchCrudeStage();

        assertAll(() -> assertEquals(1, keyPart.size()),
                () -> assertEquals(8, executorPart.size()),
                () -> assertEquals(10, variablesPart.size()),
                () -> assertEquals(35, stagesPart.size()));
    }

    @Test
    @DisplayName("Should throw exception when trying to split content of Easefile of null value")
    void fetchCrudeNullTest() {
        EasefileExtractor easefileExtractor = new MainEasefileExtractor();

        String content = null;

        assertThrows(PipelinePartCriticalError.class, () -> easefileExtractor.split(content));
    }

    @Test
    @DisplayName("Should throw exception when trying to split content of Easefile of empty value")
    void fetchCrudeEmptyTest() {
        EasefileExtractor easefileExtractor = new MainEasefileExtractor();

        String content = "";

        assertThrows(PipelinePartCriticalError.class, () -> easefileExtractor.split(content));
    }

    @Test
    @DisplayName("Should correctly read lines of well-formatted Easefile")
    void fetchCrudeStageSuccessTest() throws PipelinePartCriticalError {
        EasefileExtractor easefileExtractor = new MainEasefileExtractor();
        StageExtractor stageExtractor = (StageExtractor) easefileExtractor;

        String content = readFinalCorrectEasefile();
        easefileExtractor.split(content);

        List<Line> stagesPart = stageExtractor.fetchCrudeStage();

        assertAll(() -> assertEquals(35, stagesPart.size()));
    }
}