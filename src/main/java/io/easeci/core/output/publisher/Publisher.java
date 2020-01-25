package io.easeci.core.output.publisher;

import io.easeci.core.output.Event;

/**
 * Main object that is able to publish Events to any
 * io.easeci.core.output.topic.Topic implementation.
 * This is a functional interface that has only one method
 * so this is able only to publish Event.
 * @author Karol Meksuła
 * 2020-01-25
 * */
public interface Publisher {

    /**
     * Publish Event on specified Topic or Queue that will be consumed by subscribers.
     * @return boolean that inform us about result of Event publishing process.
     *          Returns 'false' if something was wrong and Event was not published
     *          on specified storage.
     *          Returns 'true' when successfully published Event and placed in Topic.
     * @throws PublishException when some critical error occurred and cannot
     * publish Event to specified Topic.
     * */
    public boolean publish(Event event) throws PublishException;
}
