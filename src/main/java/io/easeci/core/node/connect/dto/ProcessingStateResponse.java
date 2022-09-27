package io.easeci.core.node.connect.dto;

import io.easeci.core.node.connect.NodeProcessingState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingStateResponse {
    private NodeProcessingState nodeProcessingState;
    private Date nodeProcessingStateCheckDate;
}

