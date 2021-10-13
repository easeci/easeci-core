package io.easeci.core.engine.runtime;

import io.easeci.api.socket.Commands;
import io.easeci.core.engine.runtime.assemble.*;
import io.easeci.core.engine.runtime.commons.PipelineContextState;
import io.easeci.core.engine.runtime.commons.PipelineRunStatus;
import io.easeci.core.engine.runtime.commons.PipelineState;
import io.easeci.core.engine.runtime.logs.ArchiveLogReader;
import io.easeci.core.engine.runtime.logs.LogBuffer;
import io.easeci.core.engine.runtime.logs.LogEntry;
import io.easeci.core.engine.runtime.logs.LogRail;
import io.easeci.core.engine.scheduler.PipelineScheduler;
import io.easeci.core.workspace.LocationUtils;
import io.easeci.core.workspace.projects.ProjectManager;
import io.easeci.core.workspace.vars.GlobalVariablesFinder;
import io.easeci.core.workspace.vars.GlobalVariablesManager;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.easeci.core.engine.runtime.commons.PipelineRunStatus.PIPELINE_NOT_FOUND;
import static java.util.Objects.isNull;

@Slf4j
public class PipelineContextSystem implements PipelineRunEntryPoint, EventListener<PipelineContextInfo>,
                                              PipelineContextCloser, ArchiveLogReader {

    private static PipelineContextSystem system;

    private PipelineContextFactory factory;
    private List<PipelineContext> contextList;
    private PerformerTaskDistributor performerTaskDistributor;
    private GlobalVariablesFinder globalVariablesFinder;
    private ScriptAssembler scriptAssembler;
    private PipelineContextHistory pipelineContextHistory;
    private long maxPipelineContextLivenessTime;
    private long pipelineContextLivenessCheckInterval;
    private ScheduledExecutorService contextLivenessCheckScheduler;
    private PipelineScheduler pipelineScheduler;

    public static PipelineContextSystem getInstance() {
        if (isNull(system)) {
            system = new PipelineContextSystem();
        }
        return system;
    }

    static PipelineContextSystem getInstance(PipelineContextFactory factory) {
        if (isNull(system)) {
            system = new PipelineContextSystem(factory);
        }
        return system;
    }

    private PipelineContextSystem() {
        this.contextList = new LinkedList<>();
        this.factory = new PipelineContextFactory();
        this.performerTaskDistributor = new StandardPerformerTaskDistributor();
        this.globalVariablesFinder = GlobalVariablesManager.getInstance();
        this.scriptAssembler = new PythonScriptAssembler();
        this.pipelineContextHistory = new PipelineContextHistoryDefault();
        this.maxPipelineContextLivenessTime = this.retrieveClt();
        this.pipelineContextLivenessCheckInterval = this.retrieveLivenessCheckInterval();
        this.contextLivenessCheckScheduler = Executors.newScheduledThreadPool(1);
        this.schedulePipelineContextLivenessCheck();
    }

    private PipelineContextSystem(PipelineContextFactory factory) {
        this.contextList = new LinkedList<>();
        this.factory = factory;
        this.performerTaskDistributor = new StandardPerformerTaskDistributor();
        this.globalVariablesFinder = GlobalVariablesManager.getInstance();
        this.scriptAssembler = new PythonScriptAssembler();
        this.pipelineContextHistory = new PipelineContextHistoryDefault();
        this.maxPipelineContextLivenessTime = this.retrieveClt();
        this.pipelineContextLivenessCheckInterval = this.retrieveLivenessCheckInterval();
        this.contextLivenessCheckScheduler = Executors.newScheduledThreadPool(1);
        this.schedulePipelineContextLivenessCheck();
    }

    private long retrieveClt() {
        long clt = 60;                  // CLT default value
        try {
            clt = LocationUtils.retrieveFromGeneralInt("output.pipeline-context.clt");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        log.info("output.pipeline-context.clt = {}", clt);
        return clt;
    }

    private long retrieveLivenessCheckInterval() {
        long livenessInterval = 15;     //  liveness-check-interval default value
        try {
            livenessInterval = LocationUtils.retrieveFromGeneralInt("output.pipeline-context.liveness-check-interval");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        log.info("output.pipeline-context.liveness-check-interval = {}", livenessInterval);
        return livenessInterval;
    }

    @Override
    public PipelineRunStatus.PipelineRunStatusWrapper runPipeline(UUID pipelineId) {
        final UUID pipelineContextId = UUID.randomUUID();
        log.info("Started to run pipeline with pipelineId: {}, and pipelineContextId: {}", pipelineId, pipelineContextId);

        this.pipelineContextHistory.saveHistoricalLogEntity(pipelineContextId, pipelineId);
        LogBuffer logBuffer = new LogBuffer(pipelineId, pipelineContextId);
        logBuffer.publish(LogEntry.builder()
                .author("easeci-core-master")
                .header("[INFO]")
                .timestamp(Instant.now().getEpochSecond())
                .text("Started to run pipeline with pipelineId: " + pipelineId)
                .build());

        PipelineContext pipelineContext;
        try {
            pipelineContext = this.factory.factorize(
                    pipelineId, pipelineContextId, this,
                    performerTaskDistributor, this.globalVariablesFinder,
                    scriptAssembler, ProjectManager.getInstance(), logBuffer
            );
            pipelineContext.loadFromFile(pipelineId);
        } catch (PipelineNotExists e) {
            log.error(e.getMessage());
            return PipelineRunStatus.PipelineRunStatusWrapper.of(PIPELINE_NOT_FOUND, pipelineContextId);
        }

        log.info("New PipelineContext created for pipelineId: {}", pipelineId);
        this.contextList.add(pipelineContext);
        pipelineContext.buildScript();

        return PipelineRunStatus.PipelineRunStatusWrapper.of(PipelineRunStatus.PIPELINE_EXEC_STARTED, pipelineContextId);
    }

    @Override
    public List<PipelineContextState> contextQueueState() {
        return this.contextList.stream()
                .map(PipelineContext::state)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public LogRail getLogRail(UUID pipelineContextId) {
        return contextList.stream()
                .filter(pipelineContext -> pipelineContext.getPipelineContextId().equals(pipelineContextId))
                .map(PipelineContext::logRail)
                .findFirst()
                .orElseThrow(() -> new PipelineContextNotExists(pipelineContextId));
    }

    @Override
    public LogRail getFileLogRail(UUID pipelineContextId) {
        return pipelineContextHistory.findHistoricalLogs(pipelineContextId);
    }

    @Override
    public void receive(PipelineContextInfo event) {
        log.info("Event from PipelineContext received: {}", event.toString());

        if (PipelineState.WAITING_FOR_SCHEDULE.equals(event.getPipelineState())) {
            log.info("Pipeline with pipelineContextId: {}, is now marked as ready for scheduling", event.getPipelineContextId());
            // here we know that we can start run container with building process
        }
        else if (PipelineState.ABORTED_PREPARATION_ERROR.equals(event.getPipelineState())) {
            log.info("Pipeline with pipelineContextId: {}, is aborted now", event.getPipelineContextId());
            this.closeContext(event.getPipelineContextId());
        }
        // only when job was CLOSED by easeci-worker - pipeline was executed
        else if (PipelineState.CLOSED.equals(event.getPipelineState())) {
            this.contextList.removeIf(pipelineContext -> pipelineContext.getPipelineContextId().equals(event.getPipelineContextId()));
            log.info("PipelineContext with id: {} ends his life right now. Started at: {}, ends at: {}", event.getPipelineContextId(), event.getCreationDate(), event.getFinishDate());
        } else {
            log.error("PipelineState: {} is not handled! Omitting.", event.getPipelineState());
        }
    }

    @Override
    public void closeExpiredContexts() {
        this.contextList.forEach(pipelineContext -> {
            boolean contextExpired = pipelineContext.isMaximumIdleTimePassed(this.maxPipelineContextLivenessTime);
            if (contextExpired) {
                pipelineContext.closeContext();
            }
        });
    }

    private void closeContext(UUID pipelineContextId) {
        this.contextList.stream()
                .filter(pipelineContext -> pipelineContext.getPipelineContextId().equals(pipelineContextId))
                .findFirst()
                .ifPresent(PipelineContext::closeContext);
    }

    private void schedulePipelineContextLivenessCheck() {
        log.info("contextLivenessCheckScheduler just started");
        this.contextLivenessCheckScheduler.scheduleAtFixedRate(this::closeExpiredContexts, 0, this.pipelineContextLivenessCheckInterval, TimeUnit.SECONDS);
    }

    static void destroyInstance() {
        system = null;
    }

    @Override
    public String getArchiveFileLogRail(UUID pipelineContextId, long batchSize, int offset, Commands.LogFetchMode mode) {
        LogBuffer logBuffer = new LogBuffer();
        return logBuffer.readLog(pipelineContextId, batchSize, offset, mode);
    }
}
