package io.easeci.core.engine.runtime;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@AllArgsConstructor
public class PipelineContext implements PipelineRunnable, PipelineScriptBuilder, EventPublisher<PipelineContextInfo> {

    private EventListener<PipelineContextInfo> eventListener;

    @Override
    public PipelineContextInfo runPipeline() {
        return null;
    }

    @Override
    public void buildScript() {
        CompletableFuture.runAsync(() -> {
            log.info("Waiting for collecting script.....");
            //todo script building here
            log.info("Finished collecting script!!!");
            PipelineContextInfo info = new PipelineContextInfo();
            info.setPipelineContextId(UUID.randomUUID());
            this.publish(info);
        });
    }

    @Override
    public void publish(PipelineContextInfo event) {
        log.info("Message published to EventListener: {}", event.toString());
        this.eventListener.receive(event);
    }
}
