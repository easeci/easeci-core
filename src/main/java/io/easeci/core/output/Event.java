package io.easeci.core.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event is a representation of data published to specified instance of
 * io.easeci.core.output.topic.Topic
 * This object contains of only data fields. Be advices that instance of
 * any Event must be immutable object.
 * Event has static subclass that store meta data about event occurrence.
 * @author Karol Meksuła
 * 2020-01-25
 * */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    private String content;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventMeta {
        private EventType eventType;
        private String title;
        private LocalDateTime publishTimestamp;
        private String publishedBy;
    }
}
