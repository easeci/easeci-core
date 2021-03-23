package io.easeci.core.engine.easefile.parser.parts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.easeci.BaseWorkspaceContextTest;
import io.easeci.core.engine.easefile.parser.analyse.SyntaxError;
import io.easeci.core.engine.pipeline.EasefileObjectModel;
import io.vavr.Tuple2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static io.easeci.core.engine.easefile.parser.Utils.readEmptyMetadataTestEasefile;
import static io.easeci.core.engine.easefile.parser.parts.Feeder.*;
import static io.easeci.core.engine.easefile.parser.parts.MetadataProcessor.PROJECT_NOT_EXISTS_ERROR_TITLE;
import static org.junit.jupiter.api.Assertions.*;

public class MetadataProcessorTest extends BaseWorkspaceContextTest {

    private static final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

    @Test
    @DisplayName("Should correctly parse Easefile with simple metadata declaration")
    void baseTrialParseTest() throws PipelinePartCriticalError {
        EasefileExtractor easefileExtractor = new MainEasefileExtractor();
        String content = readEmptyMetadataTestEasefile();

        easefileExtractor.split(content);

        MetadataExtractor metadataExtractor = (MetadataExtractor) easefileExtractor;

        List<Line> lines = metadataExtractor.fetchCrudeMetadata();

        assertEquals(4, lines.size());
    }

    @Test
    @DisplayName("Should correctly parse metadata part of Easefile")
    void parseSuccessTest() {
        MetadataProcessor metadataProcessor = new MetadataProcessor(objectMapper);

        Supplier<List<Line>> lines = provideCorrectMetadata();

        Tuple2<Optional<EasefileObjectModel.Metadata>, List<SyntaxError>> result = metadataProcessor.process(lines);
        EasefileObjectModel.Metadata metadata = result._1.get();

        assertAll(() -> assertTrue(result._2.isEmpty()),
                  () -> assertEquals(0, metadata.getProjectId()),
                  () -> assertEquals("java maven", metadata.getTag()),
                  () -> assertEquals("EaseCI Production", metadata.getName()),
                  () -> assertEquals("Java project based on Maven, continuous deployment process", metadata.getDescription()),
                  () -> assertNotNull(metadata.getPipelineId()),
                  () -> assertNotNull(metadata.getLastReparseDate())
        );
    }

    @Test
    @DisplayName("Should not allow to inject restricted metadata values from Easefile for instance - pipelineId")
    void parseSuccessInjectValueTest() {
        MetadataProcessor metadataProcessor = new MetadataProcessor(objectMapper);

        Supplier<List<Line>> lines = provideCorrectMetadata2();

        Tuple2<Optional<EasefileObjectModel.Metadata>, List<SyntaxError>> result = metadataProcessor.process(lines);
        List<SyntaxError> expectedErrors = result._2;

        assertAll(() -> assertEquals(1, expectedErrors.size()),
                  () -> assertEquals(2, expectedErrors.get(0).getLineNumber()),
                  () -> assertEquals("Error occurred while Easefile parsing process", expectedErrors.get(0).getTitle()));
    }

    @Test
    @DisplayName("Should get no conflict when user type one property twice - newest property will be taken")
    void parseSuccessDoubledValueTest() {
        MetadataProcessor metadataProcessor = new MetadataProcessor(objectMapper);

        Supplier<List<Line>> lines = provideCorrectMetadata6();

        Tuple2<Optional<EasefileObjectModel.Metadata>, List<SyntaxError>> result = metadataProcessor.process(lines);
        EasefileObjectModel.Metadata metadata = result._1.get();

        assertAll(() -> assertTrue(result._2.isEmpty()),
                  () -> assertEquals(0, metadata.getProjectId()),
                  () -> assertEquals("java maven", metadata.getTag()),
                  () -> assertEquals("EaseCI Production", metadata.getName()),
                  () -> assertEquals("Java project based on Maven, continuous deployment process", metadata.getDescription()),
                  () -> assertNotNull(metadata.getPipelineId()),
                  () -> assertNotNull(metadata.getLastReparseDate())
        );
    }

    // tutaj problem jest taki, że wrzucam errory do listy, ale i tak wszystko przechodzi poprawnie.
    // Dla niektórych typów wyjątków np.: com.fasterxml.jackson.databind.exc.MismatchedInputException: No content to map due to end-of-input
    // może warto zrobić dodatkowe pole informujące, że to nie jest błąd, ale jedynie Warning ?
    @Test
    @DisplayName("Should metadata be fully optional - any data is required - empty metadata property")
    void parseSuccessEmptyMetadataTest() {
        MetadataProcessor metadataProcessor = new MetadataProcessor(objectMapper);

        Supplier<List<Line>> lines = provideCorrectMetadata4();

        Tuple2<Optional<EasefileObjectModel.Metadata>, List<SyntaxError>> result = metadataProcessor.process(lines);
        EasefileObjectModel.Metadata metadata = result._1.get();

        assertAll(() -> assertFalse(result._2.isEmpty()),
                  () -> assertNotNull(metadata));
    }

    @Test
    @DisplayName("Should no throw exception - Metadata may be not present in Easefile")
    void parseSuccessNoMetadataTest() {
        MetadataProcessor metadataProcessor = new MetadataProcessor(objectMapper);

        Supplier<List<Line>> lines = provideCorrectMetadata5();

        Tuple2<Optional<EasefileObjectModel.Metadata>, List<SyntaxError>> result = metadataProcessor.process(lines);
        EasefileObjectModel.Metadata metadata = result._1.get();

        assertAll(() -> assertTrue(result._2.isEmpty()),
                  () -> assertNotNull(metadata));
    }

    @Test
    @DisplayName("Should throw exception when trying to put Pipeline to project that not exists")
    void parseFailureProjectIdNotExistsTest() {
        MetadataProcessor metadataProcessor = new MetadataProcessor(objectMapper);

        Supplier<List<Line>> lines = provideCorrectMetadata3();

        Tuple2<Optional<EasefileObjectModel.Metadata>, List<SyntaxError>> result = metadataProcessor.process(lines);

        SyntaxError expectedError = result._2.get(0);

        assertAll(() -> assertNotNull(expectedError),
                  () -> assertEquals(2, expectedError.getLineNumber()),
                  () -> assertEquals(PROJECT_NOT_EXISTS_ERROR_TITLE, expectedError.getTitle()));
    }
}
