package io.easeci.core.log.file;

import io.easeci.core.output.Event;
import io.easeci.core.output.EventType;

import java.time.LocalDateTime;

public class EventUtils {
    public static final int EVENT_BYTE_SIZE = 117;
    public static final String EVENT_CONTENT = "This is example event content for test",
                               EVENT_TITLE = "Sample log event",
                               EVENT_PUBLISHER = "EaseCI process";
    public static final EventType EVENT_TYPE = EventType.RUNTIME;
    public static final LocalDateTime EVENT_DATE = LocalDateTime.now();

    public static Event provideEvent() {
        return Event.builder()
                .eventMeta(Event.EventMeta.builder()
                        .eventType(EVENT_TYPE)
                        .title(EVENT_TITLE)
                        .publishTimestamp(EVENT_DATE)
                        .publishedBy(EVENT_PUBLISHER)
                        .build())
                .content(EVENT_CONTENT)
                .build();
    }
}
