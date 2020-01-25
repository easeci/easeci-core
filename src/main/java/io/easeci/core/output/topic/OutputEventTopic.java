package io.easeci.core.output.topic;

import io.easeci.core.output.Event;

/**
 * Proxy interface between io.easeci.core.output.topic.Topic
 * and other implementations of this one. This interface is main
 * point when in whole Event exchange process.
 * This object tells us how event managing should works with more
 * details than Topic interface but is not concrete implementation yet.
 * @author Karol Meksuła
 * 2020-01-25
 * */
public interface OutputEventTopic extends Topic {

    /**
     * @return boolean that inform us is Event was handled or not.
     *          When 'true' value was returned, everything ends successfully
     *          and Event was stored and prepared for distributing to consumers.
     *          When 'false' value was returned, Event was not stored on queue
     *          so Event will not pass to distributing process.
     * @throws EventHandleException when critical error occurred and event
     *          handling process could not end for another reason.
     * */
    public boolean handleEvent(Event event) throws EventHandleException;
}
