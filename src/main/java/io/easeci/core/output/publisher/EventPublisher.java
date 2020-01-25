package io.easeci.core.output.publisher;

import io.easeci.core.output.topic.Topic;
import lombok.AllArgsConstructor;

/**
 * Extension of Publisher that is able to publish Events to any
 * io.easeci.core.output.topic.Topic implementation.
 * This abstract class defines some fields and methods that describes
 * more specific Topic's information.
 * @author Karol Meksuła
 * 2020-01-25
 * */
@AllArgsConstructor
public abstract class EventPublisher implements Publisher {
    private Topic topic;
}
