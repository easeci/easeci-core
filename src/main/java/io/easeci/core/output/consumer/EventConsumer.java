package io.easeci.core.output.consumer;

import io.easeci.core.output.topic.Topic;

/**
 * An Consumer extension that defines method to manage of subscription
 * to the specified Topic.
 * @author Karol Meksuła
 * 2020-01-25
 * */
public interface EventConsumer extends Consumer {

    /**
     * Subscribe any Topic and perform action commissioned by Topic.
     * @param topic method pass in argument reference to Topic that
     *              will be subscribed. If some Topic is just subscribed
     *              it will be automatically replaced will new one instance
     *              provided as an argument.
     * @return boolean that inform us about result of subscription process.
     *          Returns 'true' if subscribe was ends with success.
     *          Returns 'false' if subscribe was failed.
     * */
    public boolean subscribe(Topic topic);

    /**
     * Removes subscription of current Topic.
     * @return boolean that inform us about result of remove Topic's subscription.
     *          Returns 'true' if unsubscribe was ends with success.
     *          Returns 'false' if unsubscribe was failed.
     * */
    public boolean unsubscribe();
}
