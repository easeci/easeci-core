package io.easeci.core.output.consumer;

import io.easeci.core.output.Event;

/**
 * Functional interface that defines how the Event is consumed.
 * Notice that Topic implementation executes method of this
 * interface and returns result of that action.
 * @author Karol Meksuła
 * 2020-01-25
 * */
public interface Consumer {

    /**
     * Simply consumes resource expressed as Event object.
     * @return boolean value that describe do process ends with success
     *          or not.
     *          Returns 'true' when Event was consumed with success.
     *          Returns 'false' when Event was not consumed correctly.
     * @throws ConsumeException when some critical error occured and
     *          Consumer was not able to process event in defined way.
     * */
    public boolean consume(Event event) throws ConsumeException;
}
