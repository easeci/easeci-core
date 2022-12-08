package io.easeci.core.engine.scheduler;

import io.easeci.core.engine.runtime.PipelineContext;
import io.easeci.core.node.connect.*;
import io.easeci.core.workspace.LocationUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static io.easeci.core.engine.scheduler.ScheduleErrorCode.WORKER_NODE_NO_ANY_AVAILABLE_FOR_PROCESSING;
import static io.easeci.core.engine.scheduler.ScheduleErrorCode.WORKER_NODE_RESPONSE_NOT_SERIALIZABLE;
import static io.easeci.core.node.connect.NodeConnectionState.CONNECTION_ERROR;
import static io.easeci.core.node.connect.NodeConnectionState.NOT_CHANGED;
import static io.easeci.core.node.connect.NodeProcessingState.UNKNOWN;

@Slf4j
public class DefaultPipelineScheduler implements PipelineScheduler {

    private final ClusterNodesProvider clusterNodesProvider;
    private final ScheduleRequestPreparer scheduleRequestPreparer;
    private final NodeConnector nodeConnector;
    private final PipelineExecutionQueue pipelineExecutionQueue;

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private ScheduledFuture<?> scheduledFuture;

    public DefaultPipelineScheduler(ClusterNodesProvider clusterNodesProvider) {
        this.clusterNodesProvider = clusterNodesProvider;
        ClusterInformationDefault clusterInformationDefault = new ClusterInformationDefault();
        this.scheduleRequestPreparer = new ScheduleRequestPreparer(clusterInformationDefault);
        this.nodeConnector = new NodeConnector();
        this.pipelineExecutionQueue = new PipelineExecutionQueueDefault();
        this.scheduledFuture = this.initializeQueueMonitor();
    }

    @Override
    public ScheduleResponse schedule(PipelineContext pipelineContext) {
        Set<NodeConnection> nodes = clusterNodesProvider.getReadyToWorkNodes();
        return findNode(nodes).map(nodeConnection -> {
            log.info("Idling worker node was detected so making a request to worker node instance: {}", nodeConnection);
            return sendPipelineExecutionRequest(pipelineContext, nodeConnection);
        }).orElseGet(() -> {
            log.info("Any idling worker node was not find so PipelineContext was put in a queue");
            pipelineExecutionQueue.put(pipelineContext.queued());
            return ScheduleResponse.createResponseFailure(NOT_CHANGED, UNKNOWN, WORKER_NODE_NO_ANY_AVAILABLE_FOR_PROCESSING);
        });
    }

    private ScheduleResponse sendPipelineExecutionRequest(PipelineContext pipelineContext, NodeConnection nodeConnection) {
        final ScheduleRequest scheduleRequest = scheduleRequestPreparer.prepareRequest(pipelineContext);
        try {
            final ScheduleResponse scheduleResponse = nodeConnector.sendPipeline(nodeConnection, scheduleRequest);
            log.info("Worker node response for scheduling request: {}", scheduleResponse.toString());
            return scheduleResponse;
        } catch (UrlBuildException e) {
            log.error("Error occurred while trying to prepare request URL to node", e);
            return ScheduleResponse.createResponseFailure(CONNECTION_ERROR, UNKNOWN, WORKER_NODE_RESPONSE_NOT_SERIALIZABLE);
        } catch (NoSuchElementException e) {
            log.error("Error occurred while finding node for pipeline processing", e);
            return ScheduleResponse.createResponseFailure(CONNECTION_ERROR, UNKNOWN, WORKER_NODE_NO_ANY_AVAILABLE_FOR_PROCESSING);
        }
    }

    private Optional<NodeConnection> findNode(Set<NodeConnection> nodes) throws NoSuchElementException {
        return nodes.stream()
                .filter(nodeConnection -> NodeConnectionState.ESTABLISHED.equals(nodeConnection.getNodeConnectionState()))
                .filter(nodeConnection -> NodeProcessingState.IDLE.equals(nodeConnection.getNodeProcessingState()))
                .findAny();
    }

    private ScheduledFuture<?> initializeQueueMonitor() {
        int corePoolSize = LocationUtils.retrieveFromGeneralInt("schedule.thread-pool-execution", 1);
        int initialDelay = LocationUtils.retrieveFromGeneralInt("schedule.refresh-init-delay-seconds", 5);
        int period = LocationUtils.retrieveFromGeneralInt("schedule.refresh-interval-seconds", 5);
        if (Objects.isNull(scheduledThreadPoolExecutor)) {
            this.scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(corePoolSize);
            return run(scheduledThreadPoolExecutor, initialDelay, period);
        }
        if (scheduledFuture.isCancelled()) {
            return run(scheduledThreadPoolExecutor, initialDelay, period);
        }
        return null;
    }

    private ScheduledFuture<?> run(ScheduledThreadPoolExecutor executor, int initialDelay, int period) {
        return executor.scheduleAtFixedRate(() -> this.pipelineExecutionQueue.next()
                .ifPresent(pipelineContext -> {
                    log.info("Pipeline execution before stored on queue now re-triggered automatically for pipelineContextId: {}", pipelineContext.getPipelineContextId());
                    this.schedule(pipelineContext);
                }), initialDelay, period, TimeUnit.SECONDS);
    }
}
