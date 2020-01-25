package io.easeci.core.output;

/**
 * Enumeration describes type of Event published by any instance
 * implementing io.easeci.core.output.publisher.Publisher interface.
 * @author Karol Meksuła
 * 2020-01-25
 * */
public enum EventType {
    RUNTIME,
    PIPELINE,
    ERROR,
    SPECIAL
}
