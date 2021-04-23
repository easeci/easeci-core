package io.easeci.core.engine.runtime;

import io.easeci.core.engine.pipeline.EasefileObjectModel;
import io.easeci.core.engine.runtime.assemble.*;
import io.easeci.core.engine.runtime.commons.PipelineContextState;
import io.easeci.core.engine.runtime.commons.PipelineState;
import io.easeci.core.workspace.projects.PipelineIO;
import io.easeci.core.workspace.projects.ProjectManager;
import io.easeci.core.workspace.vars.GlobalVariablesFinder;
import io.easeci.extension.directive.CodeChunk;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static io.easeci.core.engine.runtime.commons.PipelineState.NEW;

@Slf4j
public class PipelineContext implements PipelineRunnable, PipelineScriptBuilder, EventPublisher<PipelineContextInfo> {

    private final UUID pipelineContextId;
    private final LocalDateTime contextCreatedDate;
    private final UUID pipelineId;
    private final EventListener<PipelineContextInfo> eventListener;
    private final PerformerTaskDistributor performerTaskDistributor;
    private final GlobalVariablesFinder globalVariablesFinder;
    private final ScriptAssembler scriptAssembler;
    private final EasefileObjectModel eom;
    private final PipelineIO pipelineIO;

    private String scriptAssembled;
    private PipelineState pipelineState;

    public PipelineContext(UUID pipelineId,
                           EventListener<PipelineContextInfo> eventListener,
                           PerformerTaskDistributor performerTaskDistributor,
                           GlobalVariablesFinder globalVariablesFinder,
                           ScriptAssembler scriptAssembler) throws PipelineNotExists {
        this.pipelineContextId = UUID.randomUUID();
        this.contextCreatedDate = LocalDateTime.now();
        this.pipelineId = pipelineId;
        this.eventListener = eventListener;
        this.performerTaskDistributor = performerTaskDistributor;
        this.globalVariablesFinder = globalVariablesFinder;
        this.scriptAssembler = scriptAssembler;
        this.pipelineState = NEW;
        this.pipelineIO = ProjectManager.getInstance();
        // load file from file in constructor - cannot create object when pipeline file not exists
        this.eom = this.loadFromFile(this.pipelineId);
    }

    private EasefileObjectModel loadFromFile(UUID pipelineId) throws PipelineNotExists {
        return pipelineIO.loadPipelineFile(pipelineId)
                         .orElseThrow(() -> new PipelineNotExists(pipelineId));
    }

    @Override
    public PipelineContextInfo runPipeline() {
        return null;
    }

    @Override
    public void buildScript() {
        // todo teraz napotkałem się na problem, że będziemy mieli eventy już na tym etapie
        //      bo jak chcemy wiedzieć, że na przykład zmienna się nie rozwiązała, to już tutaj musimy sobie wszystko zbierać
        //      i wrzucać na jakiś stos logów
        //      Przyda się teraz ten mechanizm logów, który już wcześniej robiłem. Wrzucamy sobie i po zakończeniu działania
        //      runtime'u danego pipeline'u zapisujemy do pliku, dzięki czemu, użytkownik będzie mógł sobie otworzyć logi w GUI
        CompletableFuture.runAsync(() -> {
            log.info("Starting collecting script chunks and waiting for all Performers to end these jobs");

            // resolve variables
            final VariableResolver variableResolver = new StandardVariableResolver(this.eom, this.globalVariablesFinder);
            EasefileObjectModel eomResolved;
            try {
                eomResolved = variableResolver.resolve();
            } catch (VariableResolveException e) {
                // todo trzeba tutaj obsłużyć ten wyjątek i publikować już jakieś logi dla tego contextu
                e.printStackTrace();
                return;
            }

            // collect all steps
            final StepsCollector stepsCollector = new StepsCollector();
            final List<PerformerCommand> performerCommands = stepsCollector.collectSteps(eomResolved.getStages());
            final List<PerformerProduct> performerProducts = new ArrayList<>(performerCommands.size());

            // call performer for each directive
            performerCommands.stream()
                    .map(this.performerTaskDistributor::callPerformer)
                    .forEach(future -> future.thenApply(performerProduct -> {
                        performerProducts.add(performerProduct);
                        return performerProduct;
                    }));

            // merge-assemble code chunks to full script
            List<CodeChunk> codeChunks = performerProducts.stream()
                    .map(PerformerProduct::getCodeChunk)
                    .collect(Collectors.toList());
            this.scriptAssembled = this.scriptAssembler.assemble(codeChunks);

            log.info("Script chunks collecting finished");
            PipelineContextInfo info = new PipelineContextInfo();
            info.setPipelineContextId(this.pipelineContextId);
            this.publish(info);
        });
    }

    @Override
    public void publish(PipelineContextInfo event) {
        log.info("Event from PipelineContext published to EventListener: {}", event.toString());
        this.eventListener.receive(event);
    }

    private Path saveScript(String fullScript) {
        // zapisz skrypt do pliku
        // zapisz sumę kontrolną -> przemyśl i stwórz odpowiedni mechanizm
        return null;
    }

    public PipelineContextState state() {
        return PipelineContextState.of(this.pipelineContextId, pipelineId, pipelineState, this.contextCreatedDate);
    }
}
