package io.easeci.core.engine.easefile.parser;

import io.easeci.core.engine.EngineStatus;
import io.easeci.core.engine.easefile.parser.analyse.StaticAnalyseException;
import io.easeci.core.engine.easefile.parser.analyse.SyntaxError;
import io.easeci.core.engine.easefile.parser.parts.PipelinePartProcessor;
import io.easeci.core.engine.pipeline.*;
import io.easeci.core.workspace.SerializeUtils;
import io.easeci.core.workspace.projects.PipelinePointerIO;
import io.vavr.Tuple2;
import lombok.Builder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static io.easeci.core.workspace.LocationUtils.getPipelineFilesLocation;

public class MainEasefileParser extends EasefileParserTemplate {

    private PipelinePartProcessor<Pipeline.Metadata> metadataProcessor;
    private PipelinePartProcessor<Key> keyProcessor;
    private PipelinePartProcessor<List<Executor>> executorsProcessor;
    private PipelinePartProcessor<List<Variable>> varsProcessor;
    private PipelinePartProcessor<List<Stage>> stagesProcessor;
    private PipelinePartProcessor<byte[]> scriptFileProcessor;

    private MainEasefileParser(PipelinePointerIO pipelinePointerIO) {
        super(pipelinePointerIO);
    }

    @Builder
    public MainEasefileParser(PipelinePointerIO pipelinePointerIO,
                              PipelinePartProcessor<Pipeline.Metadata> metadataProcessor,
                              PipelinePartProcessor<Key> keyProcessor,
                              PipelinePartProcessor<List<Executor>> executorsProcessor,
                              PipelinePartProcessor<List<Variable>> varsProcessor,
                              PipelinePartProcessor<List<Stage>> stagesProcessor,
                              PipelinePartProcessor<byte[]> scriptFileProcessor) {
        super(pipelinePointerIO);
        this.metadataProcessor = metadataProcessor;
        this.keyProcessor = keyProcessor;
        this.executorsProcessor = executorsProcessor;
        this.varsProcessor = varsProcessor;
        this.stagesProcessor = stagesProcessor;
        this.scriptFileProcessor = scriptFileProcessor;
    }

    byte[] serialize(Pipeline pipeline) {
        byte[] serialized = SerializeUtils.write(pipeline);
        return Base64.getEncoder().encode(serialized);
    }

    // todo => unit tests
    Path writePipelineFile(byte[] serializedContent) {
        Path pipelineFilesLocation = getPipelineFilesLocation();
        Path pipelineFile = Path.of(pipelineFilesLocation.toString()
                .concat("/")
                .concat("pipeline_")
                .concat(String.valueOf(System.currentTimeMillis() / 1000L)));
        try {
            Path file = Files.createFile(pipelineFile);
            Files.write(file, serializedContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pipelineFile;
    }

    Pipeline process(String easefileContent) throws StaticAnalyseException {
        Queue<SyntaxError> syntaxErrors = new ConcurrentLinkedQueue<>();

        Tuple2<Optional<Pipeline.Metadata>, List<SyntaxError>> metadata = this.metadataProcessor.process();
        Tuple2<Optional<Key>, List<SyntaxError>> key = this.keyProcessor.process();
        Tuple2<Optional<List<Executor>>, List<SyntaxError>> executors = this.executorsProcessor.process();
        Tuple2<Optional<List<Variable>>, List<SyntaxError>> variables = this.varsProcessor.process();
        Tuple2<Optional<List<Stage>>, List<SyntaxError>> stages = this.stagesProcessor.process();
        Tuple2<Optional<byte[]>, List<SyntaxError>> scriptEncoded = this.scriptFileProcessor.process();

        syntaxErrors.addAll(metadata._2);
        syntaxErrors.addAll(key._2);
        syntaxErrors.addAll(executors._2);
        syntaxErrors.addAll(variables._2);
        syntaxErrors.addAll(stages._2);
        syntaxErrors.addAll(scriptEncoded._2);

        if (syntaxErrors.isEmpty()) {
            return Pipeline.builder()
                    .metadata(metadata._1.orElse(new Pipeline.Metadata()))
                    .key(key._1.orElse(Key.of(Key.KeyType.PIPELINE)))
                    .executors(executors._1.orElse(Collections.emptyList()))
                    .variables(variables._1.orElse(Collections.emptyList()))
                    .stages(stages._1.orElse(Collections.emptyList()))
                    .scriptEncoded(scriptEncoded._1.orElse(new byte[0]))
                    .build();
        }
        throw new StaticAnalyseException(EngineStatus.S_EP_0000, new ArrayList<>(syntaxErrors));
    }

}
