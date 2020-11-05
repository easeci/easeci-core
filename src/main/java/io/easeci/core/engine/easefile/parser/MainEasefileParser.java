package io.easeci.core.engine.easefile.parser;

import io.easeci.core.engine.easefile.parser.analyse.StaticAnalyseException;

public class MainEasefileParser implements EasefileParser {

    @Override
    public EasefileParseResult parse(String easefileContent) throws StaticAnalyseException {
        System.out.println(easefileContent);
        return null;
    }
}
