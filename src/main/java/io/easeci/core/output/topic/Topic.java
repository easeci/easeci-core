package io.easeci.core.output.topic;

import io.easeci.core.output.Event;

/**
 * Functional interface that just only distribute T
 * that just be stored on object implementing this interface.
 * @author Karol Meksuła
 * 2020-01-25
 * */
public interface Topic<T extends Event> {

    /**
     * Simply distribute io.easeci.core.output.Event published before,
     * to each subscriber associated with current Topic.
     * @return <T extends Event> when distributing process ends with success then
     *          Event based object will be returned. null is never returned.
     * @throws DistributeException when distribute process was ends with failure
     * */
     T distribute() throws DistributeException;
}
