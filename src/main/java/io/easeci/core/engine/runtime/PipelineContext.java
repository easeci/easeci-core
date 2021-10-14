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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static io.easeci.core.engine.runtime.commons.PipelineState.*;
import static java.util.Objects.isNull;

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
        CompletableFuture.runAsync(() -> {
            this.startTimestamp = Instant.now().getEpochSecond();
            log.info("Starting collecting script chunks and waiting for all Performers to end these jobs, pipelineContextId: {}", pipelineContextId);
            this.logBuffer.publish(LogEntry.builder()
                                           .author("easeci-core-master")
                                           .header("[INFO]")
                                           .timestamp(this.startTimestamp)
                                           .text("Script build initialized. Starting collecting script chunks and waiting for all Performers to end these jobs")
                                           .build());

            // resolve variables
            EasefileObjectModel eomResolved;
            try {
                this.logBuffer.publish(LogEntry.builder()
                                               .author("easeci-core-master")
                                               .header("[INFO]")
                                               .timestamp(Instant.now().getEpochSecond())
                                               .text("Variables resolving started")
                                               .build());
                final VariableResolver variableResolver = new StandardVariableResolver(this.eom, this.globalVariablesFinder);
                eomResolved = variableResolver.resolve();
                log.info("Variables resolved correctly, pipelineContextId: {}", this.pipelineContextId);
                this.logBuffer.publish(LogEntry.builder()
                                               .author("easeci-core-master")
                                               .header("[INFO]")
                                               .timestamp(Instant.now().getEpochSecond())
                                               .text("Variables resolving process ends with success")
                                               .build());
            } catch (VariableResolveException e) {
                e.printStackTrace();
                this.logBuffer.publish(LogEntry.builder()
                                               .author("easeci-core-master")
                                               .header("[INFO]")
                                               .timestamp(Instant.now().getEpochSecond())
                                               .text("Variables resolving process ends with failure with message: " + e.getMessage())
                                               .build());
                this.pipelineState = ABORTED_PREPARATION_ERROR;
                this.publish(prepareContextInfo());
                return;
            } catch (Exception e) {
                e.printStackTrace();
                this.logBuffer.publish(LogEntry.builder()
                                               .author("easeci-core-master")
                                               .header("[INFO]")
                                               .timestamp(Instant.now().getEpochSecond())
                                               .text("Variables resolving process ends with critical failure with exception message: " + e.getMessage())
                                               .build());
                this.pipelineState = ABORTED_CRITICAL_ERROR;
                this.publish(prepareContextInfo());
                return;
            }

            // collect all steps
            // aggregate steps from all stages to one list
            final List<PerformerCommand> performerCommands;
            try {
                this.logBuffer.publish(LogEntry.builder()
                                               .author("easeci-core-master")
                                               .header("[INFO]")
                                               .timestamp(Instant.now().getEpochSecond())
                                               .text("Collecting steps from all stages to one entity")
                                               .build());
                final StepsCollector stepsCollector = new StepsCollector();
                performerCommands = new ArrayList<>(stepsCollector.collectSteps(eomResolved.getStages()));
                log.info("Steps collecting finished, pipelineContextId: {}", this.pipelineContextId);
                this.logBuffer.publish(LogEntry.builder()
                                               .author("easeci-core-master")
                                               .header("[INFO]")
                                               .timestamp(Instant.now().getEpochSecond())
                                               .text("Collecting steps ends successfully")
                                               .build());
            } catch (Exception e) {
                e.printStackTrace();
                this.logBuffer.publish(LogEntry.builder()
                                               .author("easeci-core-master")
                                               .header("[INFO]")
                                               .timestamp(Instant.now().getEpochSecond())
                                               .text("Collecting steps ends with critical failure with exception message: " + e.getMessage())
                                               .build());
                this.pipelineState = ABORTED_CRITICAL_ERROR;
                this.publish(prepareContextInfo());
                return;
            }

            // call performer for each directive gathered in before steps
            final List<PerformerProduct> performerProducts;
            try {
                this.logBuffer.publish(LogEntry.builder()
                                               .author("easeci-core-master")
                                               .header("[INFO]")
                                               .timestamp(Instant.now().getEpochSecond())
                                               .text("Call each performer for declared directive in Easefile")
                                               .build());
                performerProducts = performerCommands.stream()
                                                     .map(this.performerTaskDistributor::callPerformerSync)
                                                     .filter(Objects::nonNull)
                                                     .collect(Collectors.toList());
                if (performerProducts.isEmpty()) {
                    log.info("PerformerTaskDistributor finished work but there are any PerformerProduct that is not null! Processing pipelineContext aborted, pipelineContextId: {}", this.pipelineContextId);
                    this.logBuffer.publish(LogEntry.builder()
                                                   .author("easeci-core-master")
                                                   .header("[INFO]")
                                                   .timestamp(Instant.now().getEpochSecond())
                                                   .text("All Performers returns no product. Some error occurred, please check your Easefile and add some Steps or fix/reinstall Performer")
                                                   .build());
                    this.pipelineState = ABORTED_PREPARATION_ERROR;
                    PipelineContextInfo contextInfo = this.prepareContextInfo();
                    this.publish(contextInfo);
                    return;
                } else {
                    log.info("PerformerTaskDistributor finished work right now, pipelineContextId: {}", this.pipelineContextId);
                    this.logBuffer.publish(LogEntry.builder()
                                                   .author("easeci-core-master")
                                                   .header("[INFO]")
                                                   .timestamp(Instant.now().getEpochSecond())
                                                   .text("Call each performer ends successfully")
                                                   .build());
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.logBuffer.publish(LogEntry.builder()
                                               .author("easeci-core-master")
                                               .header("[INFO]")
                                               .timestamp(Instant.now().getEpochSecond())
                                               .text("Call performers step ends with critical failure with exception message: " + e.getMessage())
                                               .build());
                this.pipelineState = ABORTED_CRITICAL_ERROR;
                this.publish(prepareContextInfo());
                return;
            }

            // merge-assemble code chunks to complete list of code chunks
            List<CodeChunk> codeChunks;
            try {
                this.logBuffer.publish(LogEntry.builder()
                                               .author("easeci-core-master")
                                               .header("[INFO]")
                                               .timestamp(Instant.now().getEpochSecond())
                                               .text("Merging code chunks produced by performers into one single entity")
                                               .build());
                codeChunks = performerProducts.stream()
                                              .map(PerformerProduct::getCodeChunk)
                                              .filter(Objects::nonNull)
                                              .collect(Collectors.toList());
                if (codeChunks.isEmpty()) {
                    log.info("CodeChunks merged list is empty, so next execution steps will be aborted, pipelineContextId: {}", this.pipelineContextId);
                    this.logBuffer.publish(LogEntry.builder()
                                                   .author("easeci-core-master")
                                                   .header("[INFO]")
                                                   .timestamp(Instant.now().getEpochSecond())
                                                   .text("There are no chunks of code produced by Performers! Occurred unexpected error.")
                                                   .build());
                    this.pipelineState = ABORTED_PREPARATION_ERROR;
                    PipelineContextInfo contextInfo = this.prepareContextInfo();
                    this.publish(contextInfo);
                    return;
                } else {
                    log.info("CodeChunks now are merged to one CodeChunk list, pipelineContextId: {}", this.pipelineContextId);
                    this.logBuffer.publish(LogEntry.builder()
                                                   .author("easeci-core-master")
                                                   .header("[INFO]")
                                                   .timestamp(Instant.now().getEpochSecond())
                                                   .text("Merging code chunks ends successfully")
                                                   .build());
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.logBuffer.publish(LogEntry.builder()
                                               .author("easeci-core-master")
                                               .header("[INFO]")
                                               .timestamp(Instant.now().getEpochSecond())
                                               .text("Call performers step ends with critical failure with exception message: " + e.getMessage())
                                               .build());
                this.pipelineState = ABORTED_CRITICAL_ERROR;
                this.publish(prepareContextInfo());
                return;
            }

            // assembling complete script ready for use
            try {
                this.logBuffer.publish(LogEntry.builder()
                                               .author("easeci-core-master")
                                               .header("[INFO]")
                                               .timestamp(Instant.now().getEpochSecond())
                                               .text("Assembling executable script from result from previous steps")
                                               .build());
                log.info("Script chunks assembling started, pipelineContextId: {}", pipelineContextId);
                this.scriptAssembled = this.scriptAssembler.assemble(codeChunks);
                log.info("Script chunks collecting finished, pipelineContextId: {}", pipelineContextId);
                this.logBuffer.publish(LogEntry.builder()
                                               .author("easeci-core-master")
                                               .header("[INFO]")
                                               .timestamp(Instant.now().getEpochSecond())
                                               .text("Executable script assembled correctly")
                                               .build());
                if (isNull(this.scriptAssembled)) {
                    log.info("Assembled script is null, so next execution steps will be aborted, pipelineContextId: {}", this.pipelineContextId);
                    this.logBuffer.publish(LogEntry.builder()
                                                   .author("easeci-core-master")
                                                   .header("[INFO]")
                                                   .timestamp(Instant.now().getEpochSecond())
                                                   .text("Assembled script is null! Occurred unexpected error. Debug of ScriptAssembler.class may be required")
                                                   .build());
                    this.pipelineState = ABORTED_PREPARATION_ERROR;
                    PipelineContextInfo contextInfo = this.prepareContextInfo();
                    this.publish(contextInfo);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.logBuffer.publish(LogEntry.builder()
                                               .author("easeci-core-master")
                                               .header("[INFO]")
                                               .timestamp(Instant.now().getEpochSecond())
                                               .text("Assembling all code chunks ends with critical failure with exception message: " + e.getMessage())
                                               .build());
                this.pipelineState = ABORTED_CRITICAL_ERROR;
                this.publish(prepareContextInfo());
                return;
            }

            log.info("buildScript() method finished with no errors so, sending event to PipelineContextSystem. Now pipeline is ready and queued for scheduling process");
            // here event is publishing to PipelineContextSystem, script will be ready for scheduling
            this.publish(prepareContextInfo());
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
        this.logBuffer.publish(LogEntry.builder()
                                       .author("easeci-core-master")
                                       .header("[INFO]")
                                       .timestamp(Instant.now().getEpochSecond())
                                       .text("Executable script preparation ends with status: " + event.getPipelineState())
                                       .build());
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

    static class ScriptBuildHelper {

    }
}
