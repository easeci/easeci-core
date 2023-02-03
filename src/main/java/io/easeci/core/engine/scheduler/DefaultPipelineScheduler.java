package io.easeci.core.engine.scheduler;

import io.easeci.commons.SerializeUtils;
import io.easeci.core.engine.runtime.PipelineContext;
import io.easeci.core.node.connect.*;
import lombok.extern.slf4j.Slf4j;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

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

    public DefaultPipelineScheduler(ClusterNodesProvider clusterNodesProvider) {
        this.clusterNodesProvider = clusterNodesProvider;
        ClusterInformationDefault clusterInformationDefault = new ClusterInformationDefault();
        this.scheduleRequestPreparer = new ScheduleRequestPreparer(clusterInformationDefault);
        this.nodeConnector = new NodeConnector();
    }

    @Override
    public ScheduleResponse schedule(PipelineContext pipelineContext) {
        final Set<NodeConnection> nodes = clusterNodesProvider.getReadyToWorkNodes();
        return findNode(nodes).map(nodeConnection -> {
            log.info("Idling worker node was detected so making a request to worker node instance: {}", nodeConnection);
            return sendPipelineExecutionRequest(pipelineContext, nodeConnection);
        }).orElseGet(() -> {
            log.info("Any idling worker node was not find so PipelineContext need to be put on a queue");
            return ScheduleResponse.createResponseFailure(NOT_CHANGED, UNKNOWN, WORKER_NODE_NO_ANY_AVAILABLE_FOR_PROCESSING);
        });
    }

    private ScheduleResponse sendPipelineExecutionRequest(PipelineContext pipelineContext, NodeConnection nodeConnection) {
        final ScheduleRequest scheduleRequest = scheduleRequestPreparer.prepareRequest(pipelineContext);
        log.info("ScheduleRequest: {}", SerializeUtils.prettyWrite(scheduleRequest));
        try {
            final ScheduleResponse scheduleResponse = nodeConnector.sendPipeline(nodeConnection, scheduleRequest);
            log.info("Worker node response for scheduling request: {}", SerializeUtils.prettyWrite(scheduleResponse));
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
}
