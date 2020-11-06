package io.easeci.core.engine.easefile.loader;

import org.eclipse.jgit.api.errors.GitAPIException;

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
     * @throws IllegalAccessException when there was attempt for read file out of workspace
     * @throws GitAPIException when some error occurred when trying to load Easefile from remote git repository
     * */
    String provide() throws IOException, IllegalAccessException, GitAPIException, EasefileContentMalformed;
}

