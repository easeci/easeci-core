package io.easeci.core.engine.runtime;

public interface EventListener<T> {

    void receive(T event);
}
