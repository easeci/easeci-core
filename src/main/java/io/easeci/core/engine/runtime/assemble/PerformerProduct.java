package io.easeci.core.engine.runtime.assemble;

import io.easeci.extension.directive.CodeChunk;
import lombok.Data;

@Data
public class PerformerProduct {
    private CodeChunk codeChunk;
    private PerformerCommand performerCommand;
}
