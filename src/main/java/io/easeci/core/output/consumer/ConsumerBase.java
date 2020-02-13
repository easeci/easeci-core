package io.easeci.core.output.consumer;

import io.easeci.core.log.LogManager;
import io.easeci.core.output.Event;
import lombok.AllArgsConstructor;

/**
 * Proxy class between Consumer and its implementation.
 * As a template method this should inject by constructor
 * LogManager interface. LogManager handles Event and next
 * concrete implementation have to implement consumeEvent() method
 * @author Karol Meksuła
 * 2020-02-13
 * */

@AllArgsConstructor
public abstract class ConsumerBase implements EventConsumer {
    private LogManager logManager;

    /**
     * Template method that allows to LogManager handle event.
     * @param event is a representation of some event in application
     *              that should be persist in some way.
     * @exception ConsumeException when something went wrong and
     *                              event could not be consumed
     * */
    @Override
    public boolean consume(Event event) throws ConsumeException {
        logManager.handle(event);
        return this.consumeEvent(event);
    }

    /**
     * Proxies method of Consumer interface.
     * @param event is the same as in parent method
     * @exception ConsumeException the same as in parent method
     * */
    public abstract boolean consumeEvent(Event event) throws ConsumeException;
}
