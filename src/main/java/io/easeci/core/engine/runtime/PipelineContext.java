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
    @Getter
    private final LocalDateTime contextCreatedDate;
    @Getter
    private final UUID pipelineId;
    private final EventListener<ContextInfo> eventListener;
    private final PerformerTaskDistributor performerTaskDistributor;
    private final GlobalVariablesFinder globalVariablesFinder;
    private final ScriptAssembler scriptAssembler;
    private final PipelineIO pipelineIO;
    @Getter
    private final LogBuffer logBuffer;

    private EasefileObjectModel eom;
    private String scriptAssembled;
    @Getter
    private PipelineState pipelineState;
    @Getter
    private long startTimestamp;

    public PipelineContext(UUID pipelineId,
                           UUID pipelineContextId,
                           EventListener<ContextInfo> eventListener,
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

    public String getExecutableScript() throws IllegalStateException {
        if (isNull(scriptAssembled)) {
            throw new IllegalStateException("PipelineContext with pipelineContextId: " + pipelineContextId + " is not ready yet. Run buildScript() first.");
        }
        return scriptAssembled;
    }

    void markAsReadyForScheduling() {
        this.pipelineState = READY_FOR_SCHEDULE;
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
            info("Script build initialized. Starting collecting script chunks and waiting for all Performers to end these jobs");
            try {
                ScriptBuildHelper helper = new ScriptBuildHelper();
                EasefileObjectModel eomResolved = helper.variableResolve();
                List<PerformerCommand> performerCommands = helper.aggregatePerformerCommands(eomResolved);
                List<PerformerProduct> performerProducts = helper.callPerformerForEach(performerCommands);
                List<CodeChunk> codeChunks = helper.mergeCodeChunks(performerProducts);
                this.scriptAssembled = helper.assembleExecutableScript(codeChunks);
            } catch (ScriptBuildStepException e) {
                return;
            }
            log.info("buildScript() method finished with no errors so, sending event to PipelineContextSystem. Now pipeline is ready and queued for scheduling process. " +
                     "Assembled result script has length: " + this.scriptAssembled.length());
            this.pipelineState = WAITING_FOR_SCHEDULE;
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
        info("Executable script preparation ends with status: " + event.getPipelineState());
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
        this.info("Gently closing context with id: " + this.pipelineContextId);
        PipelineContextInfo pci = new PipelineContextInfo();
        pci.setPipelineContextId(this.pipelineContextId);
        pci.setPipelineState(this.pipelineState);
        pci.setCreationDate(Date.from(this.contextCreatedDate.atZone(ZoneId.systemDefault()).toInstant()));
        pci.setFinishDate(new Date());
        this.logBuffer.closeLogging();
        this.eventListener.receive(pci);
    }

    // log to output available for user
    void error(String message) {
        logBuffer.publish(LogEntry.builder()
                                  .author("easeci-core-master")
                                  .header("[ERROR]")
                                  .timestamp(Instant.now().getEpochSecond())
                                  .text(message)
                                  .build());
    }

    // log to output available for user
    void info(String message) {
        logBuffer.publish(LogEntry.builder()
                                  .author("easeci-core-master")
                                  .header("[INFO]")
                                  .timestamp(Instant.now().getEpochSecond())
                                  .text(message)
                                  .build());
    }

    public PipelineContext queued() {
        this.pipelineState = QUEUED;
        return this;
    }

    private class ScriptBuildHelper {

        // resolve variables declared in Easefile
        EasefileObjectModel variableResolve() throws ScriptBuildStepException {
            try {
                info("Variables resolving started");
                final VariableResolver variableResolver = new StandardVariableResolver(eom, globalVariablesFinder);
                final EasefileObjectModel eomResolved = variableResolver.resolve();
                log.info("Variables resolved correctly, pipelineContextId: {}", pipelineContextId);
                info("Variables resolving process ends with success");
                return eomResolved;
            } catch (VariableResolveException e) {
                e.printStackTrace();
                error("Variables resolving process ends with failure with message: " + e.getMessage());
                pipelineState = ABORTED_PREPARATION_ERROR;
                publish(prepareContextInfo());
                throw new ScriptBuildStepException("variableResolve");
            } catch (Exception e) {
                e.printStackTrace();
                error("Variables resolving process ends with critical failure with exception message: " + e.getMessage());
                pipelineState = ABORTED_CRITICAL_ERROR;
                publish(prepareContextInfo());
                throw new ScriptBuildStepException("variableResolve");
            }
        }

        // collect all steps
        // aggregate steps from all stages to one list
        List<PerformerCommand> aggregatePerformerCommands(EasefileObjectModel eomResolved) throws ScriptBuildStepException {
            try {
                info("Collecting steps from all stages to one entity");
                final StepsCollector stepsCollector = new StepsCollector();
                final List<PerformerCommand> performerCommands = new ArrayList<>(stepsCollector.collectSteps(eomResolved.getStages()));
                log.info("Steps collecting finished, pipelineContextId: {}", pipelineContextId);
                info("Collecting steps ends successfully");
                return performerCommands;
            } catch (Exception e) {
                e.printStackTrace();
                error("Collecting steps ends with critical failure with exception message: " + e.getMessage());
                pipelineState = ABORTED_CRITICAL_ERROR;
                publish(prepareContextInfo());
                throw new ScriptBuildStepException("aggregatePerformerCommands");
            }
        }

        // call performer for each directive gathered in before steps
        List<PerformerProduct> callPerformerForEach(List<PerformerCommand> performerCommands) throws ScriptBuildStepException {
            final List<PerformerProduct> performerProducts;
            try {
                info("Call each performer for declared directive in Easefile");
                performerProducts = performerCommands.stream()
                        .map(performerTaskDistributor::callPerformerSync)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                if (performerProducts.isEmpty()) {
                    log.info("PerformerTaskDistributor finished work but there are any PerformerProduct that is not null! Processing pipelineContext aborted, pipelineContextId: {}", pipelineContextId);
                    error("All Performers returns no product. Some error occurred, please check your Easefile and add some Steps or fix/reinstall Performer");
                    pipelineState = ABORTED_PREPARATION_ERROR;
                    PipelineContextInfo contextInfo = prepareContextInfo();
                    publish(contextInfo);
                    throw new ScriptBuildStepException("callPerformerForEach");
                } else if (performerProducts.size() != performerCommands.size()) {
                    log.info("PerformerTaskDistributor finished work but desired PerformerProduct (size={}) is not equal to PerformerCommand (size={}) size!! Processing pipelineContext aborted, pipelineContextId: {}",
                            performerProducts.size(), performerCommands.size(), pipelineContextId);
                    error("PerformerTaskDistributor finished work but desired PerformerProduct (size="
                            + performerProducts.size()+") is not equal to PerformerCommand (size="
                            + performerCommands.size()+") size!!. Some error occurred, please check your Easefile and add some Steps or fix/reinstall Performer");
                    pipelineState = ABORTED_PREPARATION_ERROR;
                    PipelineContextInfo contextInfo = prepareContextInfo();
                    publish(contextInfo);
                    throw new ScriptBuildStepException("callPerformerForEach");
                } else {
                    log.info("PerformerTaskDistributor finished work right now, pipelineContextId: {}", pipelineContextId);
                    info("Call each performer ends successfully");
                    return performerProducts;
                }
            } catch (Exception e) {
                e.printStackTrace();
                error("Call performers step ends with critical failure with exception message: " + e.getMessage());
                pipelineState = ABORTED_CRITICAL_ERROR;
                publish(prepareContextInfo());
                throw new ScriptBuildStepException("callPerformerForEach");
            }
        }

        // merge-assemble code chunks to complete list of code chunks
        public List<CodeChunk> mergeCodeChunks(List<PerformerProduct> performerProducts) throws ScriptBuildStepException {
            List<CodeChunk> codeChunks;
            try {
                info("Merging code chunks produced by performers into one single entity");
                codeChunks = performerProducts.stream()
                        .map(PerformerProduct::getCodeChunk)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                if (codeChunks.isEmpty()) {
                    log.info("CodeChunks merged list is empty, so next execution steps will be aborted, pipelineContextId: {}", pipelineContextId);
                    error("There are no chunks of code produced by Performers! Occurred unexpected error.");
                    pipelineState = ABORTED_PREPARATION_ERROR;
                    PipelineContextInfo contextInfo = prepareContextInfo();
                    publish(contextInfo);
                    throw new ScriptBuildStepException("mergeCodeChunks");
                } else {
                    log.info("CodeChunks now are merged to one CodeChunk list, pipelineContextId: {}", pipelineContextId);
                    info("Merging code chunks ends successfully");
                    return codeChunks;
                }
            } catch (Exception e) {
                e.printStackTrace();
                info("Call performers step ends with critical failure with exception message: " + e.getMessage());
                pipelineState = ABORTED_CRITICAL_ERROR;
                publish(prepareContextInfo());
                throw new ScriptBuildStepException("mergeCodeChunks");
            }
        }

        // assembling complete script ready for use
        public String assembleExecutableScript(List<CodeChunk> codeChunks) throws ScriptBuildStepException {
            try {
                info("Assembling executable script from result from previous steps");
                log.info("Script chunks assembling started, pipelineContextId: {}", pipelineContextId);
                final String scriptAssembled = scriptAssembler.assemble(codeChunks);
                log.info("Script chunks collecting finished, pipelineContextId: {}", pipelineContextId);
                info("Executable script assembled correctly");
                if (isNull(scriptAssembled)) {
                    log.info("Assembled script is null, so next execution steps will be aborted, pipelineContextId: {}", pipelineContextId);
                    error("Assembled script is null! Occurred unexpected error. Debug of ScriptAssembler.class may be required");
                    pipelineState = ABORTED_PREPARATION_ERROR;
                    PipelineContextInfo contextInfo = prepareContextInfo();
                    publish(contextInfo);
                    throw new ScriptBuildStepException("assembleExecutableScript");
                }
                return scriptAssembled;
            } catch (Exception e) {
                e.printStackTrace();
                error("Assembling all code chunks ends with critical failure with exception message: " + e.getMessage());
                pipelineState = ABORTED_CRITICAL_ERROR;
                publish(prepareContextInfo());
                throw new ScriptBuildStepException("assembleExecutableScript");
            }
        }
    }

    private static class ScriptBuildStepException extends Exception {

        private final String stepMethodName;

        ScriptBuildStepException(String stepMethodName) {
            this.stepMethodName = stepMethodName;
        }

        @Override
        public String getMessage() {
            return "Script building failed on step: " + this.stepMethodName;
        }
    }
}
