package io.easeci.core.engine.easefile.parser.parts.tools;

import io.easeci.core.engine.easefile.parser.analyse.SyntaxError;
import io.easeci.core.engine.easefile.parser.parts.Line;
import io.easeci.extension.command.VariableType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Abstract class that extracts - compose data structure from concrete lines of plain text.
 *
 * @author Karol Meksuła
 * 2021-02-16
 * */
public abstract class DataStructureConstructor<T> {
    private List<Line> bufferedLines;
    private VariableType variableType;
    private boolean inputEnds;

    public DataStructureConstructor() {
        this.bufferedLines = new ArrayList<>(0);
        this.inputEnds = false;
    }

    // store data
    public abstract void buffer(Line line);

    public VariableType inferType() {
        // TODO: 16.02.2021
        return null;
    }

    /**
     * Use this to terminate work of instance of this class.
     * VariableExtractor may construct data structure correctly or not.
     * Decision is yours.
     * */
    public void signalEndOfInput() {
        this.inputEnds = true;
    }

    /**
     * Use this method only in this package. Implementation of this abstract method
     * should inform us about end of collection data for initialization data structure.
     * @return true if there is end of input - all lines was read.
     *         false if there was no end of input yet and cannot create instance of collection or something,
     *                  May occurs for instance where array was open '[', but there was no close bracket ']'
     * */
    abstract boolean endOfInput();

    abstract T result();

    public Optional<T> getResult() {
        if (this.inputEnds) {
            return Optional.ofNullable(this.result());
        }
        return Optional.empty();
    }


    public List<SyntaxError> getErrors() {
        return Collections.emptyList();
    }
}
