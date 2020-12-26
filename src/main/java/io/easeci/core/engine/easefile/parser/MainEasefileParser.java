package io.easeci.core.engine.easefile.parser;

import io.easeci.core.engine.EngineStatus;
import io.easeci.core.engine.easefile.parser.analyse.StaticAnalyseException;
import io.easeci.core.engine.easefile.parser.analyse.SyntaxError;
import io.easeci.core.engine.easefile.parser.parts.*;
import io.easeci.core.engine.pipeline.*;
import io.easeci.core.workspace.SerializeUtils;
import io.easeci.core.workspace.projects.PipelinePointerIO;
import io.easeci.core.workspace.vars.Variable;
import io.vavr.Tuple2;
import lombok.Builder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static io.easeci.core.workspace.LocationUtils.getPipelineFilesLocation;
import static java.util.Objects.isNull;

class MainEasefileParser extends EasefileParserTemplate {

    private PipelinePartProcessor<Pipeline.Metadata> metadataProcessor;
    private PipelinePartProcessor<Key> keyProcessor;
    private PipelinePartProcessor<List<Executor>> executorsProcessor;
    private PipelinePartProcessor<List<Variable>> varsProcessor;
    private PipelinePartProcessor<List<Stage>> stagesProcessor;
    private PipelinePartProcessor<byte[]> scriptFileProcessor;
    private EasefileExtractor easefileExtractor;

    @Builder
    MainEasefileParser(PipelinePointerIO pipelinePointerIO,
                       EasefileExtractor easefileExtractor,
                       PipelinePartProcessor<Pipeline.Metadata> metadataProcessor,
                       PipelinePartProcessor<Key> keyProcessor,
                       PipelinePartProcessor<List<Executor>> executorsProcessor,
                       PipelinePartProcessor<List<Variable>> varsProcessor,
                       PipelinePartProcessor<List<Stage>> stagesProcessor,
                       PipelinePartProcessor<byte[]> scriptFileProcessor) {
        super(pipelinePointerIO);
        this.easefileExtractor = easefileExtractor;
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

    Path writePipelineFile(byte[] serializedContent) {
        Path pipelineFilesLocation = getPipelineFilesLocation();
        Path pipelineFile = Path.of(pipelineFilesLocation.toString()
                .concat("/")
                .concat("pipeline_")
                .concat(String.valueOf(System.currentTimeMillis())));
        try {
            Path file = Files.createFile(pipelineFile);
            Files.write(file, serializedContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pipelineFile;
    }

    @Override
    Pipeline process(String easefileContent) throws StaticAnalyseException, PipelinePartCriticalError {
        Queue<SyntaxError> syntaxErrors = new ConcurrentLinkedQueue<>();

        easefileExtractor.split(easefileContent);

        Tuple2<Optional<Pipeline.Metadata>, List<SyntaxError>> metadata = this.metadataProcessor.process(() -> ((MetadataExtractor) easefileExtractor).fetchCrudeMetadata());
        Tuple2<Optional<Key>, List<SyntaxError>> key = this.keyProcessor.process(() -> ((KeyExtractor) easefileExtractor).fetchCrudeKey());
        Tuple2<Optional<List<Executor>>, List<SyntaxError>> executors = this.executorsProcessor.process(() -> ((ExecutorExtractor) easefileExtractor).fetchCrudeExecutor());
        Tuple2<Optional<List<Variable>>, List<SyntaxError>> variables = this.varsProcessor.process(() -> ((VariableExtractor) easefileExtractor).fetchCrudeVariable());
        Tuple2<Optional<List<Stage>>, List<SyntaxError>> stages = this.stagesProcessor.process(() -> ((StageExtractor) easefileExtractor).fetchCrudeStage());
        Tuple2<Optional<byte[]>, List<SyntaxError>> scriptEncoded = this.scriptFileProcessor.process(() -> null);

        validateProcessingResult(metadata, "Metadata");
        validateProcessingResult(key, "Key");
        validateProcessingResult(executors, "Executors");
        validateProcessingResult(variables, "Variables");
        validateProcessingResult(stages, "Stages");
        validateProcessingResult(scriptEncoded, "Script collecting");

        collectErrors(metadata, syntaxErrors);
        collectErrors(key, syntaxErrors);
        collectErrors(executors, syntaxErrors);
        collectErrors(variables, syntaxErrors);
        collectErrors(stages, syntaxErrors);
        collectErrors(scriptEncoded, syntaxErrors);

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

    private <T> void collectErrors(Tuple2<Optional<T>, List<SyntaxError>> tuple, Queue<SyntaxError> syntaxErrors) {
        Optional.ofNullable(tuple)
                .map(tpl -> tpl._2)
                .ifPresent(syntaxErrors::addAll);
    }

    private <T> void validateProcessingResult(Tuple2<Optional<T>, List<SyntaxError>> processingResult, final String processorName) throws PipelinePartCriticalError {
        if (isNull(processingResult)) {
            throw new PipelinePartCriticalError(List.of(
                    ParsingError.of(
                            processorName + " Pipeline part processor is damaged",
                            "Please check is your instance of EaseCI Core has correctly defined plugins or call EaseCI support",
                            processorName + " Pipeline part processor may be not initialized, " +
                                    "EasefileParser can be instantiate in wrong way " +
                                    "or external directive parser (plugin) is broken and cannot return processable result"
                    )
            ));
        }
    }
}
