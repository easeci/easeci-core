package io.easeci.core.engine.runtime.assemble;

import io.easeci.core.extension.DirectivesCollector;
import io.easeci.extension.directive.CodeChunk;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class StandardPerformerTaskDistributor implements PerformerTaskDistributor {

    private DirectivesCollector directivesCollector;

    public StandardPerformerTaskDistributor(DirectivesCollector directivesCollector) {
        this.directivesCollector = directivesCollector;
    }

    @Override
    public CompletableFuture<PerformerProduct> callPerformer(PerformerCommand performerCommand) {
        return null;
    }

    @Override
    public PerformerProduct callPerformerSync(PerformerCommand performerCommand) {
        return directivesCollector.find(performerCommand.getDirectiveName())
                .map(directive -> {
                    try {
                        CodeChunk codeChunk = directive.provideCode(performerCommand.getInvocationBody());
                        return PerformerProduct.of(codeChunk, performerCommand);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        return null;
                    }
                }).orElseGet(() -> {
                    log.error("Performer call failed. Performer was found but this not return any code. {}", performerCommand);
                    return null;
                });
    }
}
