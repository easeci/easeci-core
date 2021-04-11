package io.easeci.core.engine.runtime.assemble;

import io.easeci.extension.directive.CodeChunk;

import java.util.List;

public interface ScriptAssembler {

    String assemble(List<CodeChunk> codeChunkList);
}
