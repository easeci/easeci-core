package io.easeci.core.engine.easefile.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.easeci.BaseWorkspaceContextTest;
import io.easeci.core.engine.pipeline.EasefileObjectModel;
import io.easeci.core.workspace.projects.ProjectManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static io.easeci.core.engine.easefile.parser.Utils.provideEmptyPipelineForTest;
import static org.junit.jupiter.api.Assertions.*;

class MainEasefileParserTest extends BaseWorkspaceContextTest {

    @Test
    @DisplayName("Should correctly serialize and deserialize pipeline file to base64 encoded form")
    void serializePipelineObjectTest() throws IOException {
        MainEasefileParser parser = (MainEasefileParser) ParserFactory.factorize(ParserFactory.ParserType.STANDARD);

        EasefileObjectModel pipeline = provideEmptyPipelineForTest();

        byte[] serialized = parser.serialize(pipeline);

        byte[] decode = Base64.getDecoder().decode(serialized);
        ObjectMapper objectMapper = new ObjectMapper();

        EasefileObjectModel pipelineDeserialized = objectMapper.readValue(decode, EasefileObjectModel.class);

        assertAll(() -> assertEquals(pipeline.getMetadata(), pipelineDeserialized.getMetadata()),
                () -> assertEquals(pipeline.getKey(), pipelineDeserialized.getKey()),
                () -> assertEquals(pipeline.getExecutorConfiguration(), pipelineDeserialized.getExecutorConfiguration()),
                () -> assertEquals(pipeline.getVariables(), pipelineDeserialized.getVariables()),
                () -> assertEquals(pipeline.getStages(), pipelineDeserialized.getStages()),
                () -> assertEquals(pipeline.getScriptEncoded().length, pipelineDeserialized.getScriptEncoded().length)
        );
    }

    @Test
    @DisplayName("Should correctly save pipeline file as serialized and base64 encoded file's content")
    void writePipelineFileTest() {
        ProjectManager.destroyInstance();
        ProjectManager.getInstance();

        MainEasefileParser parser = (MainEasefileParser) ParserFactory.factorize(ParserFactory.ParserType.STANDARD);

        EasefileObjectModel pipeline = provideEmptyPipelineForTest();
        byte[] serializedPipeline = parser.serialize(pipeline);
        final Path emptyPipelineFile = parser.createEmptyPipelineFile();
        final Path path = parser.writePipelineFile(emptyPipelineFile, serializedPipeline);

        boolean isFileExists = Files.exists(path);

        assertAll(() -> assertNotNull(parser),
                () -> assertNotNull(serializedPipeline),
                () -> assertNotNull(parser),
                () -> assertTrue(isFileExists));
    }
}