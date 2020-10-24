package io.easeci.extension.directive;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "of")
public class CodeChunk {
    private CodeLanguage language;
    private String code;
    private String encoding;
}
