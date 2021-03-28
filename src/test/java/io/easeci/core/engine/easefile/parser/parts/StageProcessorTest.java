package io.easeci.core.engine.easefile.parser.parts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.easeci.core.engine.easefile.parser.analyse.SyntaxError;
import io.easeci.core.engine.pipeline.Stage;
import io.easeci.extension.command.VariableType;
import io.vavr.Tuple2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static io.easeci.core.engine.easefile.parser.parts.Feeder.provideCorrectFlow;
import static org.junit.jupiter.api.Assertions.*;

class StageProcessorTest {

    private static final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

    @Test
    @DisplayName("Should correctly parse simple flow: property and retrieve all stages")
    void successTest() {
        StageProcessor stageProcessor = new StageProcessor(objectMapper);

        Supplier<List<Line>> lines = provideCorrectFlow();
        List<Line> finalLinesUnwrap = lines.get();
        Supplier<List<Line>> linesTruncated = () -> lines.get().subList(1, finalLinesUnwrap.size());

        Tuple2<Optional<List<Stage>>, List<SyntaxError>> result = stageProcessor.process(linesTruncated);

        List<Stage> stages = result._1.orElseThrow();

        final Stage firstStage = stages.get(0);
        final Stage secondStage = stages.get(1);
        final Stage thirdStage = stages.get(2);
        final Stage fourthStage = stages.get(3);
        final Stage fifthStage = stages.get(4);
        final Stage sixthStage = stages.get(5);
        final Stage seventhStage = stages.get(6);

        assertAll(
                () -> assertTrue(result._2().isEmpty()),
                () -> assertEquals(7, stages.size()),

                () -> assertEquals(0, firstStage.getOrder()),
                () -> assertEquals("Prepare building environment", firstStage.getName()),
                () -> assertEquals(1, firstStage.getSteps().size()),
                () -> assertNull(firstStage.getVariables()),
                () -> assertEquals(0, firstStage.getSteps().get(0).getOrder()),
                () -> assertEquals("$ssh", firstStage.getSteps().get(0).getDirectiveName()),
                () -> assertEquals("mkdir -p {_repo_clone_target}", firstStage.getSteps().get(0).getInvocationBody()),

                () -> assertEquals(1, secondStage.getOrder()),
                () -> assertEquals("Preparation of project building", secondStage.getName()),
                () -> assertEquals(1, secondStage.getSteps().size()),
                () -> assertNull(secondStage.getVariables()),
                () -> assertEquals(0, secondStage.getSteps().get(0).getOrder()),
                () -> assertEquals("$git", secondStage.getSteps().get(0).getDirectiveName()),
                () -> assertEquals("clone {_repo_address}", secondStage.getSteps().get(0).getInvocationBody()),

                () -> assertEquals(2, thirdStage.getOrder()),
                () -> assertEquals("Unit tests", thirdStage.getName()),
                () -> assertEquals(2, thirdStage.getSteps().size()),
                () -> assertNull(thirdStage.getVariables()),
                () -> assertEquals(0, thirdStage.getSteps().get(0).getOrder()),
                () -> assertEquals("$mvn", thirdStage.getSteps().get(0).getDirectiveName()),
                () -> assertEquals("test", thirdStage.getSteps().get(0).getInvocationBody()),
                () -> assertEquals(1, thirdStage.getSteps().get(1).getOrder()),
                () -> assertEquals("$bash", thirdStage.getSteps().get(1).getDirectiveName()),
                () -> assertEquals("cp -r target/test-result/* /tmp/logs/", thirdStage.getSteps().get(1).getInvocationBody()),

                () -> assertEquals(3, fourthStage.getOrder()),
                () -> assertEquals("Building project", fourthStage.getName()),
                () -> assertEquals(1, fourthStage.getSteps().size()),
                () -> assertNull(fourthStage.getVariables()),
                () -> assertEquals(0, fourthStage.getSteps().get(0).getOrder()),
                () -> assertEquals("$mvn", fourthStage.getSteps().get(0).getDirectiveName()),
                () -> assertEquals("install", fourthStage.getSteps().get(0).getInvocationBody()),

                () -> assertEquals(4, fifthStage.getOrder()),
                () -> assertEquals("Publish artifact", fifthStage.getName()),
                () -> assertEquals(1, fifthStage.getSteps().size()),
                () -> assertNull(fifthStage.getVariables()),
                () -> assertEquals(0, fifthStage.getSteps().get(0).getOrder()),
                () -> assertEquals("$artifactory", fifthStage.getSteps().get(0).getDirectiveName()),
                () -> assertEquals("{_repo_clone_target} {_artifactory_url}", fifthStage.getSteps().get(0).getInvocationBody()),

                () -> assertEquals(5, sixthStage.getOrder()),
                () -> assertEquals("Deploy to development env", sixthStage.getName()),
                () -> assertEquals(1, sixthStage.getSteps().size()),
                () -> assertNull(sixthStage.getVariables()),
                () -> assertEquals(0, sixthStage.getSteps().get(0).getOrder()),
                () -> assertEquals("$deploy", sixthStage.getSteps().get(0).getDirectiveName()),
                () -> assertEquals("ssh {_dev_hosts}", sixthStage.getSteps().get(0).getInvocationBody()),

                () -> assertEquals(6, seventhStage.getOrder()),
                () -> assertEquals("Deploy to production env", seventhStage.getName()),
                () -> assertEquals(2, seventhStage.getSteps().size()),
                () -> assertNotNull(seventhStage.getVariables()),
                () -> assertEquals(1, seventhStage.getVariables().size()),
                () -> assertEquals("log_dir", seventhStage.getVariables().get(0).getName()),
                () -> assertEquals(VariableType.STRING, seventhStage.getVariables().get(0).getType()),
                () -> assertEquals("/tmp/logs/", seventhStage.getVariables().get(0).getValue()),

                () -> assertEquals(0, seventhStage.getSteps().get(0).getOrder()),
                () -> assertEquals("$bash", seventhStage.getSteps().get(0).getDirectiveName()),
                () -> assertEquals(100, seventhStage.getSteps().get(0).getInvocationBody().length()),

                () -> assertEquals(1, seventhStage.getSteps().get(1).getOrder()),
                () -> assertEquals("$bash", seventhStage.getSteps().get(1).getDirectiveName()),
                () -> assertEquals(107, seventhStage.getSteps().get(1).getInvocationBody().length())
        );
    }

}