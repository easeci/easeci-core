package io.easeci.core.engine.easefile.parser;

import io.easeci.core.engine.EngineStatus;
import io.easeci.core.engine.easefile.parser.analyse.StaticAnalyseException;
import io.easeci.core.engine.easefile.parser.analyse.SyntaxError;
import io.easeci.core.engine.easefile.parser.parts.*;
import io.easeci.core.engine.pipeline.*;
import io.easeci.core.workspace.projects.PipelineIO;
import io.easeci.core.workspace.projects.PipelinePointerIO;
import io.easeci.core.workspace.vars.Variable;
import io.vavr.Tuple2;
import lombok.Builder;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.util.Objects.isNull;

class MainEasefileParser extends EasefileParserTemplate {

    private PipelinePartProcessor<EasefileObjectModel.Metadata> metadataProcessor;
    private PipelinePartProcessor<Key> keyProcessor;
    private PipelinePartProcessor<ExecutorConfiguration> executorsProcessor;
    private PipelinePartProcessor<List<Variable>> varsProcessor;
    private PipelinePartProcessor<List<Stage>> stagesProcessor;
    private PipelinePartProcessor<byte[]> scriptFileProcessor;
    private EasefileExtractor easefileExtractor;
    private PipelineIO pipelineIO;

    @Builder
    MainEasefileParser(PipelinePointerIO pipelinePointerIO,
                       EasefileExtractor easefileExtractor,
                       PipelinePartProcessor<EasefileObjectModel.Metadata> metadataProcessor,
                       PipelinePartProcessor<Key> keyProcessor,
                       PipelinePartProcessor<ExecutorConfiguration> executorsProcessor,
                       PipelinePartProcessor<List<Variable>> varsProcessor,
                       PipelinePartProcessor<List<Stage>> stagesProcessor,
                       PipelinePartProcessor<byte[]> scriptFileProcessor,
                       PipelineIO pipelineIO) {
        super(pipelinePointerIO);
        this.easefileExtractor = easefileExtractor;
        this.metadataProcessor = metadataProcessor;
        this.keyProcessor = keyProcessor;
        this.executorsProcessor = executorsProcessor;
        this.varsProcessor = varsProcessor;
        this.stagesProcessor = stagesProcessor;
        this.scriptFileProcessor = scriptFileProcessor;
        this.pipelineIO = pipelineIO;
    }

    @Override
    Path createEmptyPipelineFile() {
        return this.pipelineIO.createPipelineFile();
    }

    @Override
    Path writePipelineFile(Path pipelineFile, EasefileObjectModel easefileObjectModel) {
        return this.pipelineIO.writePipelineFile(pipelineFile, easefileObjectModel);
    }

    @Override
    EasefileObjectModel process(String easefileContent) throws StaticAnalyseException, PipelinePartCriticalError {
        Queue<SyntaxError> syntaxErrors = new ConcurrentLinkedQueue<>();

        easefileExtractor.split(easefileContent);

        Tuple2<Optional<EasefileObjectModel.Metadata>, List<SyntaxError>> metadata = this.metadataProcessor.process(() -> ((MetadataExtractor) easefileExtractor).fetchCrudeMetadata());
        Tuple2<Optional<Key>, List<SyntaxError>> key = this.keyProcessor.process(() -> ((KeyExtractor) easefileExtractor).fetchCrudeKey());
        Tuple2<Optional<ExecutorConfiguration>, List<SyntaxError>> executors = this.executorsProcessor.process(() -> ((ExecutorExtractor) easefileExtractor).fetchCrudeExecutor());
        Tuple2<Optional<List<Variable>>, List<SyntaxError>> variables = this.varsProcessor.process(() -> ((VariableExtractor) easefileExtractor).fetchCrudeVariable());
        Tuple2<Optional<List<Stage>>, List<SyntaxError>> stages = this.stagesProcessor.process(() -> ((StageExtractor) easefileExtractor).fetchCrudeStage());
//        Tuple2<Optional<byte[]>, List<SyntaxError>> scriptEncoded = this.scriptFileProcessor.process(() -> null);

        validateProcessingResult(metadata, "Metadata");
        validateProcessingResult(key, "Key");
        validateProcessingResult(executors, "Executors");
        validateProcessingResult(variables, "Variables");
        validateProcessingResult(stages, "Stages");
//        validateProcessingResult(scriptEncoded, "Script collecting");

        collectErrors(metadata, syntaxErrors);
        collectErrors(key, syntaxErrors);
        collectErrors(executors, syntaxErrors);
        collectErrors(variables, syntaxErrors);
        collectErrors(stages, syntaxErrors);
//        collectErrors(scriptEncoded, syntaxErrors);

        if (syntaxErrors.isEmpty()) {
            return EasefileObjectModel.builder()
                    .metadata(metadata._1.orElse(new EasefileObjectModel.Metadata()))
                    .key(key._1.orElse(Key.of(Key.KeyType.PIPELINE)))
                    .executorConfiguration(executors._1.orElse(new ExecutorConfiguration()))
                    .variables(variables._1.orElse(Collections.emptyList()))
                    .stages(stages._1.orElse(Collections.emptyList()))
//                    .scriptEncoded(scriptEncoded._1.orElse(new byte[0]))
                    .build();
        }
        throw new StaticAnalyseException(EngineStatus.F_EP_0002, new ArrayList<>(syntaxErrors));
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
