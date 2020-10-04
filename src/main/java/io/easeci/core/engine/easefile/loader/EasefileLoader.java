package io.easeci.core.engine.easefile.loader;

/**
 * Interface responsible for provide Easefile's content
 * to application in order to further processing.
 * @author Karol Meksuła
 * 2020-10-04
 * */
public interface EasefileLoader<T> {

    /**
     * Invoke this when you want to receive whole
     * content of your Easefile.
     * @param reference is an information where Easefile can be found
     *                  May it be String.class, Path.class, URL.class etc.
     *                  It depends on specific implementation of this contract.
     * @return string representation of Easefile's content
     * */
    String provide(T reference);
}

