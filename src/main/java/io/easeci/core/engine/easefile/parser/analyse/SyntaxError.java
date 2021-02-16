package io.easeci.core.engine.easefile.parser.analyse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.nio.file.Path;

@Getter
@Builder
@ToString
@AllArgsConstructor
public class SyntaxError {
    private Path errorFilePath;
    private int lineNumber;
    private String title;
    private String info;
}
