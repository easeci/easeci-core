package io.easeci.core.engine.scheduler;

import io.easeci.core.engine.runtime.PipelineContext;
import io.easeci.core.node.connect.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

import static io.easeci.core.engine.scheduler.ScheduleErrorCode.WORKER_NODE_RESPONSE_NOT_SERIALIZABLE;
import static io.easeci.core.node.connect.NodeConnectionState.CONNECTION_ERROR;
import static io.easeci.core.node.connect.NodeProcessingState.UNKNOWN;

@Slf4j
public class DefaultPipelineScheduler implements PipelineScheduler {

    private final ClusterNodesProvider clusterNodesProvider;
    private final ScheduleRequestPreparer scheduleRequestPreparer;
    private final NodeConnector nodeConnector;

    public DefaultPipelineScheduler(ClusterNodesProvider clusterNodesProvider) {
        this.clusterNodesProvider = clusterNodesProvider;
        ClusterInformationDefault clusterInformationDefault = new ClusterInformationDefault();
        this.scheduleRequestPreparer = new ScheduleRequestPreparer(clusterInformationDefault);
        this.nodeConnector = new NodeConnector();
    }

    @Override
    public ScheduleResult schedule(PipelineContext pipelineContext) {
        log.info("Scheduler instance received pipelineContext with pipelineContextId: {} for scheduling process", pipelineContext.getPipelineContextId());
        ScheduleRequest scheduleRequest = scheduleRequestPreparer.prepareRequest(pipelineContext);
        Set<NodeConnection> nodes = clusterNodesProvider.getReadyToWorkNodes();

        NodeConnection nodeConnectionChosen = nodes.stream()
                .filter(nodeConnection -> NodeConnectionState.ESTABLISHED.equals(nodeConnection.getNodeConnectionState()))
                .filter(nodeConnection -> NodeProcessingState.IDLE.equals(nodeConnection.getNodeProcessingState()))
                .findAny()
                .orElseThrow();

        try {
            return nodeConnector.sendPipeline(nodeConnectionChosen, scheduleRequest);
        } catch (UrlBuildException e) {
            log.error("Error occurred while trying to prepare request URL to node", e);
            return ScheduleResult.createResponseFailure(CONNECTION_ERROR, UNKNOWN, WORKER_NODE_RESPONSE_NOT_SERIALIZABLE);
        }
    }
}
