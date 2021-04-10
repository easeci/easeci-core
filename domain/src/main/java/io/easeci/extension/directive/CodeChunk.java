package io.easeci.extension.directive;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "of")
public class CodeChunk {
    private int order;
    private String directiveName;
    private CodeLanguage language;
    private String code;
    private String encoding;
}
