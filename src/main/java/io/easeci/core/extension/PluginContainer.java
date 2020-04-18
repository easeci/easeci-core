package io.easeci.core.extension;

import java.util.List;
import java.util.function.Predicate;

/**
 * Representation of container for hold all references
 * to object instantiated from external implementation
 * of extension-api.
 * Notice that PluginContainer could be accessed from few
 * threads so it must be thread-safety.
 * @author Karol Meksuła
 * 2020-03-27
 * */
interface PluginContainer {

    /**
     * Add implementation one of the interface from extension-api
     * and store this one in container.
     * @param instance is object that represents all required data to
     *                 managing plugin in runtime.
     * */
    void add(Instance instance);

    /**
     * Get specific implementation for one of interface specified
     * in extension-api.
     * @param interfaceName is a path of interface in extension-api
     *                      for example: (restrict a format!)
     *                      io.easeci.extension.bootstrap.OnStartup
     * @param type is a class's type that you want this method to return
     * @return instance of class stored before in container of type like
     *                      it is specified in second method's argument
     * */
    <T> T getSpecific(String interfaceName, Class<T> type);

    /**
     * Get all implementations of Standalone plugins from extension-api.
     * Standalone plugins is plugin's type that could be run next of
     * main application flow.
     * @param interfaceName is a path of interface in extension-api
     *                      for example: (restrict a format!)
     *                      io.easeci.extension.bootstrap.OnStartup
     * @param type is a class's type that you want this method to return
     * @return all instances of class stored before in container of type like
     *                      it is specified in second method's argument
     * */
    <T> List<T> getGathered(String interfaceName, Class<T> type);

    /**
     * Removes instance from container.
     * @param interfaceName is a path of interface in extension-api
     *                      for example: (restrict a format!)
     *                      io.easeci.extension.bootstrap.OnStartup
     * @param toRemove is predicate that describes condition when
     *                 specified instance should be popped from container.
     * @param type is a class's type that you want this method to return
     * @return instance that was popped from container.
     * */
    <T> T remove(String interfaceName, Predicate<Object> toRemove, Class<T> type);

    /**
     * Get information of current state of container
     * @return POJO representation of container's information.
     * */
    PluginContainerState state();

    /**
     * @return size of current container's key set
     * */
    int size();

    /**
     * @return size of instance of specific interface
     * */
    int implementationSize(String interfaceName);
}
