package io.easeci.core.engine.runtime;

import io.easeci.core.engine.pipeline.EasefileObjectModel;
import io.easeci.core.engine.runtime.assemble.*;
import io.easeci.core.engine.runtime.commons.PipelineContextState;
import io.easeci.core.engine.runtime.commons.PipelineState;
import io.easeci.core.engine.runtime.logs.LogBuffer;
import io.easeci.core.engine.runtime.logs.LogEntry;
import io.easeci.core.engine.runtime.logs.LogRail;
import io.easeci.core.workspace.projects.PipelineIO;
import io.easeci.core.workspace.vars.GlobalVariablesFinder;
import io.easeci.extension.directive.CodeChunk;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static io.easeci.core.engine.runtime.commons.PipelineState.*;

@Slf4j
public class PipelineContext implements PipelineRunnable, PipelineScriptBuilder, EventPublisher<PipelineContextInfo>,
                                        PipelineContextLivenessProbe {

    @Getter
    private final UUID pipelineContextId;
    private final LocalDateTime contextCreatedDate;
    private final UUID pipelineId;
    private final EventListener<PipelineContextInfo> eventListener;
    private final PerformerTaskDistributor performerTaskDistributor;
    private final GlobalVariablesFinder globalVariablesFinder;
    private final ScriptAssembler scriptAssembler;
    private final PipelineIO pipelineIO;

    private EasefileObjectModel eom;
    private String scriptAssembled;
    private PipelineState pipelineState;
    private LogBuffer logBuffer;
    private long startTimestamp;

    public PipelineContext(UUID pipelineId,
                           UUID pipelineContextId,
                           EventListener<PipelineContextInfo> eventListener,
                           PerformerTaskDistributor performerTaskDistributor,
                           GlobalVariablesFinder globalVariablesFinder,
                           ScriptAssembler scriptAssembler,
                           PipelineIO pipelineIO,
                           LogBuffer logBuffer) throws PipelineNotExists {
        this.pipelineContextId = pipelineContextId;
        this.contextCreatedDate = LocalDateTime.now();
        this.pipelineId = pipelineId;
        this.eventListener = eventListener;
        this.performerTaskDistributor = performerTaskDistributor;
        this.globalVariablesFinder = globalVariablesFinder;
        this.scriptAssembler = scriptAssembler;
        this.pipelineState = NEW;
        this.pipelineIO = pipelineIO;
        this.logBuffer = logBuffer;
    }

    // load file from file in constructor - cannot create object when pipeline file not exists
    public EasefileObjectModel loadFromFile(UUID pipelineId) throws PipelineNotExists {
        return pipelineIO.loadPipelineFile(pipelineId)
                         .map(easefileObjectModel -> {
                             this.eom = easefileObjectModel;
                             return easefileObjectModel;
                         })
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
            this.startTimestamp = Instant.now().getEpochSecond();
            log.info("Starting collecting script chunks and waiting for all Performers to end these jobs, pipelineContextId: {}", pipelineContextId);
            logBuffer.publish(LogEntry.builder()
                                  .author("easeci-core-master")
                                  .header("[INFO]")
                                  .timestamp(this.startTimestamp)
                                  .text("Starting collecting script chunks and waiting for all Performers to end these jobs")
                                  .build());

            // resolve variables
            EasefileObjectModel eomResolved;
            try {
                final VariableResolver variableResolver = new StandardVariableResolver(this.eom, this.globalVariablesFinder);
                eomResolved = variableResolver.resolve();
                log.info("Variables resolved correctly, pipelineContextId: {}", pipelineContextId);
            } catch (VariableResolveException e) {
                // todo trzeba tutaj obsłużyć ten wyjątek i publikować już jakieś logi dla tego contextu
                // oprócz tego, wyślij event, że sory, ale pipeline się nie zbudował.
                e.printStackTrace();
                return;
            } catch (Exception e) {
                e.printStackTrace();
                // tutaj wysłać event, że wystąpił niezidentyfikowany błąd
                return;
            }

            // collect all steps
            // aggregate steps from all stages to one list
            final List<PerformerProduct> performerProducts;
            final List<PerformerCommand> performerCommands;
            try {
                final StepsCollector stepsCollector = new StepsCollector();
                performerCommands = new ArrayList<>(stepsCollector.collectSteps(eomResolved.getStages()));
                log.info("Steps collecting finished, pipelineContextId: {}", pipelineContextId);
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            // call performer for each directive gathered in before steps
            try {
                performerProducts = performerCommands.stream()
                        .map(this.performerTaskDistributor::callPerformerSync)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                log.info("PerformerTaskDistributor finished work right now, pipelineContextId: {}", pipelineContextId);
                if (performerProducts.isEmpty()) {
                    log.info("PerformerTaskDistributor finished work but there are any PerformerProduct that is not null! Processing pipelineContext aborted, pipelineContextId: {}", pipelineContextId);
                    this.pipelineState = ABORTED_PREPARATION_ERROR;
                    PipelineContextInfo contextInfo = this.prepareContextInfo();
                    this.publish(contextInfo);
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            // merge-assemble code chunks to complete list of code chunks
            List<CodeChunk> codeChunks;
            try {
                codeChunks = performerProducts.stream()
                        .map(PerformerProduct::getCodeChunk)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                log.info("CodeChunks now are merged to one CodeChunk list, pipelineContextId: {}", pipelineContextId);
                if (codeChunks.isEmpty()) {
                    log.info("CodeChunks merged list is empty, so next execution steps will be aborted, pipelineContextId: {}", pipelineContextId);
                    this.pipelineState = ABORTED_PREPARATION_ERROR;
                    PipelineContextInfo contextInfo = this.prepareContextInfo();
                    this.publish(contextInfo);
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            // assembling complete script ready for use
            try {
                log.info("Script chunks assembling started, pipelineContextId: {}", pipelineContextId);
                this.scriptAssembled = this.scriptAssembler.assemble(codeChunks);
                log.info("Script chunks collecting finished, pipelineContextId: {}", pipelineContextId);
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            log.info("buildScript() method finished with no errors so, sending event to PipelineContextSystem. Now pipeline is ready and queued for scheduling process");
            // here event is publishing to PipelineContextSystem, script will be ready for scheduling
            PipelineContextInfo info = this.prepareContextInfo();
            this.publish(info);
        });
    }

    private PipelineContextInfo prepareContextInfo() {
        PipelineContextInfo info = new PipelineContextInfo();
        info.setCreationDate(new Date(startTimestamp));
        info.setPipelineContextId(this.pipelineContextId);
        info.setPipelineState(this.pipelineState);
        return info;
    }

    @Override
    public void publish(PipelineContextInfo event) {
        log.info("Event from PipelineContext published to EventListener: {}", event.toString());
        this.eventListener.receive(event);
    }

    @Override
    public boolean isMaximumIdleTimePassed(long clt) {
        return this.logBuffer.isMaximumIdleTimePassed(clt);
    }

    public PipelineContextState state() {
        return PipelineContextState.of(this.pipelineContextId, pipelineId, pipelineState, this.contextCreatedDate.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond());
    }

    public LogRail logRail() {
        return logBuffer;
    }

    public void closeContext() {
        this.pipelineState = CLOSED;
        log.info("Gently closing context with id: {}", this.pipelineContextId);
        PipelineContextInfo pci = new PipelineContextInfo();
        pci.setPipelineContextId(this.pipelineContextId);
        pci.setPipelineState(this.pipelineState);
        pci.setCreationDate(Date.from(this.contextCreatedDate.atZone(ZoneId.systemDefault()).toInstant()));
        pci.setFinishDate(new Date());
        this.logBuffer.closeLogging();
        this.eventListener.receive(pci);
    }
}
