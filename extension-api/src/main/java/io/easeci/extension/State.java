package io.easeci.extension;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class State {
    private LocalDateTime startDateTime;
    private LocalDateTime stopDateTime;
    private boolean isRunning;
    private final String threadName = Thread.currentThread().getName();
}
