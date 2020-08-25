package io.easeci.core.log;

import io.easeci.core.output.Event;
import io.easeci.core.output.EventType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static io.easeci.core.log.Publishers.SYSTEM;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplicationLevelLogFacade {
    private final static ApplicationLevelLog applicationLevelLog = ApplicationLevelLog.getInstance();

    public static void note(String title, String content) {
        applicationLevelLog.handle(Event.builder()
                .eventMeta(Event.EventMeta.builder()
                        .eventType(EventType.API)
                        .publishedBy(SYSTEM.name())
                        .publishTimestamp(LocalDateTime.now())
                        .title(title)
                        .build())
                .content(content)
                .build());
    }
}
