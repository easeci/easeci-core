package io.easeci.core.engine.easefile.parser;

/**
 * Functional interface that provide entry point for
 * static analyse and parsing pipeline file called Easefile
 * This is the main point where whole pipeline processing starts.
 * @author Karol Meksuła
 * 2020-09-29
 * */
public interface EasefileParser {

    /**
     * Use this method to parse provided Easefile.
     * This should get as input plain textfile and process it to return Java object.
     * @param easefileContent is a String representation
     *                        of Easefile loaded to system
     * @return EasefileParseResult is wrapper and contains EasefileInput
     *         that is parsed Easefile as a POJO representation.
     *         This object gathers and holds all information provided in
     *         Easefile and require for whole pipeline process.
     * */
    EasefileParseResult parse(String easefileContent);
}
