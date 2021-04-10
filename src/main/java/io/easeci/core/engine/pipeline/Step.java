package io.easeci.core.engine.pipeline;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Step {
    private int order;
    private String directiveName;
    private String invocationBody;
}
