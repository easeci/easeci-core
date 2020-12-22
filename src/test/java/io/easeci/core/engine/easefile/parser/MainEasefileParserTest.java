package io.easeci.core.engine.easefile.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.easeci.BaseWorkspaceContextTest;
import io.easeci.core.engine.pipeline.Pipeline;
import io.easeci.core.workspace.projects.ProjectManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Base64;

import static io.easeci.core.engine.easefile.parser.Utils.provideEmptyPipelineForTest;
import static org.junit.jupiter.api.Assertions.*;

class MainEasefileParserTest extends BaseWorkspaceContextTest {

    @Test
    @DisplayName("Should correctly serialize and deserialize pipeline file to base64 encoded form")
    void serializePipelineObjectTest() throws IOException {
        MainEasefileParser parser = (MainEasefileParser) ParserFactory.factorize(ParserFactory.ParserType.STANDARD);

        Pipeline pipeline = provideEmptyPipelineForTest();

        byte[] serialized = parser.serialize(pipeline);

        byte[] decode = Base64.getDecoder().decode(serialized);
        ObjectMapper objectMapper = new ObjectMapper();

        Pipeline pipelineDeserialized = objectMapper.readValue(decode, Pipeline.class);

        assertAll(() -> assertEquals(pipeline.getMetadata(), pipelineDeserialized.getMetadata()),
                () -> assertEquals(pipeline.getKey(), pipelineDeserialized.getKey()),
                () -> assertEquals(pipeline.getExecutors(), pipelineDeserialized.getExecutors()),
                () -> assertEquals(pipeline.getVariables(), pipelineDeserialized.getVariables()),
                () -> assertEquals(pipeline.getStages(), pipelineDeserialized.getStages()),
                () -> assertEquals(pipeline.getScriptEncoded().length, pipelineDeserialized.getScriptEncoded().length)
        );
    }

    @Test
    @DisplayName("Should correctly save pipeline file as serialized and base64 encoded file's content")
    void writePipelineFileTest() {
        ProjectManager.getInstance();

        MainEasefileParser parser = (MainEasefileParser) ParserFactory.factorize(ParserFactory.ParserType.STANDARD);

        Pipeline pipeline = provideEmptyPipelineForTest();
        byte[] serializedPipeline = parser.serialize(pipeline);
        Path path = parser.writePipelineFile(serializedPipeline);
        System.out.println(path);

        assertAll(() -> assertNotNull(parser));
    }
}