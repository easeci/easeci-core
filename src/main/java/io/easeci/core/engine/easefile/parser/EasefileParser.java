package io.easeci.core.engine.easefile.parser;

import io.easeci.core.engine.easefile.EasefileInput;
import io.easeci.core.engine.easefile.parser.analyse.StaticAnalyseException;

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
     * @return EasefileInput is parsed Easefile as a POJO representation.
     *         This object gathers and holds all information provided in
     *         Easefile and require for whole pipeline process.
     * @throws StaticAnalyseException that contains all errors detected in Easefile file.
     *         This exception should contains list of errors with line where occurred, message etc.
     * */
    EasefileInput parse(String easefileContent) throws StaticAnalyseException;
}
