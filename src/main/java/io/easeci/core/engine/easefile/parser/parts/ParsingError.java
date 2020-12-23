package io.easeci.core.engine.easefile.parser.parts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor(staticName = "of")
public class ParsingError {
    private String title;
    private String info;
    private String cause;
}
