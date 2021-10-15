package io.easeci.core.engine.runtime.assemble;

import io.easeci.extension.directive.CodeChunk;

import java.util.List;
import java.util.stream.Collectors;

public class PythonScriptAssembler implements ScriptAssembler {

    @Override
    public String assemble(List<CodeChunk> codeChunkList) {
        return codeChunkList.stream()
                .map(CodeChunk::getCode)
                .collect(Collectors.joining("\n"));
    }
}
