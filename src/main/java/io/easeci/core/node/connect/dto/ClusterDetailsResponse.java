package io.easeci.core.node.connect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class ClusterDetailsResponse {
    private List<ClusterNodeDetails> clusterNodes;
}

