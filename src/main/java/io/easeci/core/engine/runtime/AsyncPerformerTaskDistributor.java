package io.easeci.core.engine.runtime;

import io.easeci.extension.directive.CodeChunk;

import java.util.concurrent.CompletableFuture;

public class AsyncPerformerTaskDistributor implements PerformerTaskDistributor {

    @Override
    public CompletableFuture<CodeChunk> callPerformer(String performerName, String params) {
        return null;
    }
}
