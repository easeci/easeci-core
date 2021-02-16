package io.easeci.core.engine.pipeline;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor(staticName = "of")
public class Executor {
    private UUID nodeUuid;
}
