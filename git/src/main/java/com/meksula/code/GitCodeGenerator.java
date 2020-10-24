package com.meksula.code;

import io.easeci.extension.command.Command;
import io.easeci.extension.directive.CodeChunk;
import io.easeci.extension.directive.CodeLanguage;

import java.util.List;

public class GitCodeGenerator {
    private List<Command> commands;

    public CodeChunk generate(List<Command> commands) {
        // todo
        return CodeChunk.of(CodeLanguage.BASH, "", "UTF-8");
    }
}
