package io.easeci.core.engine.runtime;

public interface EventPublisher<V> {

    void publish(V event);
}
