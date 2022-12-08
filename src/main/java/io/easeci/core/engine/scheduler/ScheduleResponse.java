package io.easeci.core.engine.scheduler;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.easeci.core.node.connect.NodeConnectionState;
import io.easeci.core.node.connect.NodeProcessingState;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {
    private NodeConnectionState nodeConnectionState;
    private NodeProcessingState nodeProcessingState;
    /**
     * Worker Node should return this field,
     * indicates time point when Worker started processing pipeline
     * */
    private long pipelineReceivedTime;
    private ScheduleErrorCode scheduleErrorCode;
    @JsonProperty(value = "isSuccessfullyScheduled")
    private boolean isSuccessfullyScheduled;

    private ScheduleResponse(NodeConnectionState nodeConnectionState, NodeProcessingState nodeProcessingState,
                           ScheduleErrorCode scheduleErrorCode) {
        this.nodeConnectionState = nodeConnectionState;
        this.nodeProcessingState = nodeProcessingState;
        this.scheduleErrorCode = scheduleErrorCode;
    }

    public static ScheduleResponse createResponseFailure(NodeConnectionState nodeConnectionState, NodeProcessingState nodeProcessingState,
                                                       ScheduleErrorCode scheduleErrorCode) {
        return new ScheduleResponse(nodeConnectionState, nodeProcessingState, scheduleErrorCode);
    }
}
