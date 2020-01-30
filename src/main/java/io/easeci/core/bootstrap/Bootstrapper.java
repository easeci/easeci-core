package io.easeci.core.bootstrap;

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
     * */
    void bootstrap(String[] args);
}
