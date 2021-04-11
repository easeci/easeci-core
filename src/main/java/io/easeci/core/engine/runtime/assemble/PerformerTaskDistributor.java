package io.easeci.core.engine.runtime.assemble;

import io.easeci.extension.directive.CodeChunk;

import java.util.concurrent.CompletableFuture;

public interface PerformerTaskDistributor {

    CompletableFuture<CodeChunk> callPerformer(String performerName, String params);
}
