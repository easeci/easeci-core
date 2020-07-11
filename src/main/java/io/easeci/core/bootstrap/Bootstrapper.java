package io.easeci.core.bootstrap;

import io.easeci.core.extension.PluginSystemCriticalException;

/**
 * Functional interface with void method. Only one role of this
 * object is bootstrapping and initializing whole application
 * context separated from main(..) method.
 * @author Karol Meksuła
 * 2020-01-30
 * */
public interface Bootstrapper {

    /**
     * Handle .jar execution arguments and starting to bootstrapping
     * process of application context.
     * @param args is the same arguments as you got in main() method.
     * */
    void bootstrap(String[] args) throws PluginSystemCriticalException;
}
