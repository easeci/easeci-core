package io.easeci.core.engine.runtime.assemble;

import java.util.concurrent.CompletableFuture;

public interface PerformerTaskDistributor {

    CompletableFuture<PerformerProduct> callPerformer(PerformerCommand performerCommand);
}
