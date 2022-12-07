package io.easeci.core.engine.scheduler;

import io.easeci.core.node.connect.NodeConnectionState;
import io.easeci.core.node.connect.NodeProcessingState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResult {
    private NodeConnectionState nodeConnectionState;
    private NodeProcessingState nodeProcessingState;
    /**
     * Worker Node should return this field,
     * indicates time point when Worker started processing pipeline
     * */
    private Instant pipelineReceivedTime;
    private ScheduleErrorCode scheduleErrorCode;

    private ScheduleResult(NodeConnectionState nodeConnectionState, NodeProcessingState nodeProcessingState,
                           ScheduleErrorCode scheduleErrorCode) {
        this.nodeConnectionState = nodeConnectionState;
        this.nodeProcessingState = nodeProcessingState;
        this.scheduleErrorCode = scheduleErrorCode;
    }

    public static ScheduleResult createResponseFailure(NodeConnectionState nodeConnectionState, NodeProcessingState nodeProcessingState,
                                                       ScheduleErrorCode scheduleErrorCode) {
        return new ScheduleResult(nodeConnectionState, nodeProcessingState, scheduleErrorCode);
    }
}
