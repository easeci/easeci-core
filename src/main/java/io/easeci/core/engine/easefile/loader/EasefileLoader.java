package io.easeci.core.engine.easefile.loader;

import java.io.IOException;

/**
 * Interface responsible for provide Easefile's content
 * to application in order to further processing.
 * @author Karol Meksuła
 * 2020-10-04
 * */
public interface EasefileLoader {

    /**
     * Invoke this when you want to receive whole
     * content of your Easefile.
     * @return string representation of Easefile's content
     * @throws IOException when cannot load file from some defined source
     * */
    String provide() throws IOException, IllegalAccessException;
}

