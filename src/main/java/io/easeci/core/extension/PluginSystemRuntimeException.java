package io.easeci.core.extension;

import io.easeci.core.log.ApplicationLevelLog;
import io.easeci.core.output.Event;
import io.easeci.core.output.EventType;

import java.time.LocalDateTime;

public class PluginSystemRuntimeException extends RuntimeException {

    PluginSystemRuntimeException(Event event) {
        super(event.getContent());

        ApplicationLevelLog.getInstance().handle(event);
    }

    PluginSystemRuntimeException(String message) {
        super(message);

        ApplicationLevelLog.getInstance()
                .handle(Event.builder()
                        .content(message)
                        .eventMeta(Event.EventMeta.builder()
                                .eventType(EventType.PLUGIN_SYSTEM)
                                .title("PluginSystemRuntimeException was thrown")
                                .publishTimestamp(LocalDateTime.now())
                                .publishedBy(this.getClass().getName())
                                .build())
                        .build());
    }
}
