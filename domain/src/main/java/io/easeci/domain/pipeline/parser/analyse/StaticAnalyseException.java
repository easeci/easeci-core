package io.easeci.domain.pipeline.parser.analyse;

import java.util.List;

public class StaticAnalyseException extends Exception {
    private final List<SyntaxError> syntaxErrorList;

    public StaticAnalyseException(List<SyntaxError> syntaxErrorList) {
        this.syntaxErrorList = syntaxErrorList;
    }

    public List<SyntaxError> getSyntaxErrorList() {
        return syntaxErrorList;
    }
}
