package io.easeci.core.engine.runtime.assemble;

import io.easeci.extension.directive.CodeChunk;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "of")
public class PerformerProduct {
    private CodeChunk codeChunk;
    private PerformerCommand performerCommand;
}
