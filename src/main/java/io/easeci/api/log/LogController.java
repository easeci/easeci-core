package io.easeci.api.log;

import io.easeci.api.log.dto.EventRequest;
import io.easeci.core.log.ApplicationLevelLog;
import io.easeci.core.log.LogManager;
import io.easeci.core.output.Event;
import io.easeci.core.output.EventType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/log")
class LogController {
    private LogManager logManager = ApplicationLevelLog.getInstance();

    @PostMapping
    void addEvent(@RequestBody EventRequest request) {
        Event event = Event.builder()
                .eventMeta(Event.EventMeta.builder()
                        .publishedBy("API request")
                        .title(request.getTitle())
                        .publishTimestamp(LocalDateTime.now())
                        .eventType(EventType.API)
                        .build())
                .content(request.getContent())
                .build();

        logManager.handle(event);
    }
}
