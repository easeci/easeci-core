package io.easeci.core.engine.easefile.parser;

import io.easeci.core.engine.easefile.parser.parts.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ParserFactoryTest {

    @Test
    @DisplayName("Should create correctly initialized object")
    void factorizeEasefileParser() throws NoSuchFieldException, IllegalAccessException {
        EasefileParser easefileParser = ParserFactory.factorize(ParserFactory.ParserType.STANDARD);

        Class<? extends EasefileParser> objectClass = easefileParser.getClass();
        Field metadataProcessorField = objectClass.getDeclaredField("metadataProcessor");
        metadataProcessorField.setAccessible(true);
        MetadataProcessor metadataProcessor = (MetadataProcessor) metadataProcessorField.get(easefileParser);

        Field keyProcessorField = objectClass.getDeclaredField("keyProcessor");
        keyProcessorField.setAccessible(true);
        KeyProcessor keyProcessor = (KeyProcessor) keyProcessorField.get(easefileParser);

        Field executorsProcessorField = objectClass.getDeclaredField("executorsProcessor");
        executorsProcessorField.setAccessible(true);
        ExecutorProcessor executorProcessor = (ExecutorProcessor) executorsProcessorField.get(easefileParser);

        Field varsProcessorField = objectClass.getDeclaredField("varsProcessor");
        varsProcessorField.setAccessible(true);
        VariableProcessor variableProcessor = (VariableProcessor) varsProcessorField.get(easefileParser);

        Field stagesProcessorField = objectClass.getDeclaredField("stagesProcessor");
        stagesProcessorField.setAccessible(true);
        StageProcessor stageProcessor = (StageProcessor) stagesProcessorField.get(easefileParser);

        Field scriptFileProcessorField = objectClass.getDeclaredField("scriptFileProcessor");
        scriptFileProcessorField.setAccessible(true);
        ScriptFileProcessor scriptFileProcessor = (ScriptFileProcessor) scriptFileProcessorField.get(easefileParser);

        assertAll(() -> assertNotNull(metadataProcessor),
                () -> assertNotNull(keyProcessor),
                () -> assertNotNull(executorProcessor),
                () -> assertNotNull(variableProcessor),
                () -> assertNotNull(stageProcessor),
                () -> assertNotNull(scriptFileProcessor)
        );
    }
}
