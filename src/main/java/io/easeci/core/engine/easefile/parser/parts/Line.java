package io.easeci.core.engine.easefile.parser.parts;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class Line {
    private final int lineNumber;
    private final String content;

    @Override
    public String toString() {
        return "Line:" + lineNumber + " " + content;
    }
}
