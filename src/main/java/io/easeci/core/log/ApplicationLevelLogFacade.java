package io.easeci.core.log;

import io.easeci.core.output.Event;
import io.easeci.core.output.EventType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import static io.easeci.core.log.Publishers.SYSTEM;

@Slf4j
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

    public static void logit(LogLevelName logLevelName, String content, LogLevelPrefix prefix) {
        log.info(prefix.toPrefix() + " " + content);
        applicationLevelLog.handle(Event.builder()
                .eventMeta(Event.EventMeta.builder()
                        .eventType(EventType.API)
                        .publishedBy(SYSTEM.name())
                        .publishTimestamp(LocalDateTime.now())
                        .title(logLevelName.toLevelName())
                        .build())
                .content(content)
                .build());
    }

    public static void logit(LogLevelName logLevelName, String content) {
        log.info(LogLevelPrefix.FIVE.toPrefix() + " " + content);
        applicationLevelLog.handle(Event.builder()
                .eventMeta(Event.EventMeta.builder()
                        .eventType(EventType.API)
                        .publishedBy(SYSTEM.name())
                        .publishTimestamp(LocalDateTime.now())
                        .title(logLevelName.toLevelName())
                        .build())
                .content(content)
                .build());
    }

    /**
     * This enum was created in order to pointing event.
     * Thanks for that we could (for example) exclude some types of logs.
     * */
    public enum LogLevelName {
        PLUGIN_EVENT {
            @Override
            public String toLevelName() {
                return "Plugin Event";
            }
        },
        WORKSPACE_EVENT {
            @Override
            public String toLevelName() {
                return "Workspace Event";
            }
        },
        EASEFILE_EVENT {
            @Override
            public String toLevelName() {
                return "Easefile Event";
            }
        },
        TECHNICAL_EVENT {
            @Override
            public String toLevelName() {
                return "Technical Event";
            }
        };
        public abstract String toLevelName();
    }

    public enum LogLevelPrefix {
        ONE {
            @Override public String toPrefix() {
                return "=>";
            }
        },
        TWO {
            @Override public String toPrefix() {
                return "==>";
            }
        },
        THREE {
            @Override public String toPrefix() {
                return "===>";
            }
        },
        FOUR {
            @Override public String toPrefix() {
                return "====>";
            }
        },
        FIVE {
            @Override public String toPrefix() {
                return "=====>";
            }
        },
        SIX {
            @Override public String toPrefix() {
                return "======>";
            }
        };
        public abstract String toPrefix();
    }
}
