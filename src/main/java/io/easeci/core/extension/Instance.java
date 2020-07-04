package io.easeci.core.extension;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import static java.util.Objects.nonNull;

@Slf4j
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PACKAGE)
public class Instance {
    public Thread thread;
    public final LocalDateTime instantiateDateTime = LocalDateTime.now();
    private Plugin plugin;
    private Object instance;

    public synchronized void assignThread(Thread thread) {
        if (this.thread == null) {
            this.thread = thread;
            log.info("Thread " + thread.getName() + " is assigned now to plugin: " + plugin.toShortString());
        }
        else log.error("====> Instance has thread assigned! Cannot change.");
    }

    public boolean isRunning() {
        return nonNull(this.getInstance()) &&
               nonNull(this.getInstantiateDateTime()) &&
               nonNull(this.thread.getName());
    }

    @Override
    public boolean equals(Object obj) {
        return this.plugin.equals(((Instance) obj).plugin);
    }

    @Override
    public int hashCode() {
        return this.plugin.hashCode();
    }
}
