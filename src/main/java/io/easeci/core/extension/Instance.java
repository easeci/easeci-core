package io.easeci.core.extension;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PACKAGE)
public class Instance {
    public final String THREAD_NAME = Thread.currentThread().getName();
    public final LocalDateTime instantiateDateTime = LocalDateTime.now();
    private Plugin plugin;
    private Object instance;

    @Override
    public boolean equals(Object obj) {
        return this.plugin.equals(((Instance) obj).plugin);
    }

    @Override
    public int hashCode() {
        return this.plugin.hashCode();
    }
}
