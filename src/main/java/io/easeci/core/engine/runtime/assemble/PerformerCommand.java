package io.easeci.core.engine.runtime.assemble;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor(staticName = "of")
public class PerformerCommand {
    // for information purposes only
    private int _stageOrder;
    private int _stepOrder;

    // the parameter determines the order of execution in pipeline flow
    private int order;
    private String directiveName;
    private String invocationBody;
}