package io.easeci.core.engine.runtime.assemble;

import java.util.concurrent.CompletableFuture;

public class StandardPerformerTaskDistributor implements PerformerTaskDistributor {

    @Override
    public CompletableFuture<PerformerProduct> callPerformer(PerformerCommand performerCommand) {
        return null;
    }

    @Override
    public PerformerProduct callPerformerSync(PerformerCommand performerCommand) {
        return null;
    }
}
