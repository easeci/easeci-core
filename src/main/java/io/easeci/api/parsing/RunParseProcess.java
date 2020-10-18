package io.easeci.api.parsing;

import io.easeci.core.engine.easefile.loader.Source;
import lombok.Data;

@Data
public class RunParseProcess {
    private Source source;
    private String localStoragePath;
    private String gitRepositoryUrl;
    private String encodedEasefileContent;
}
