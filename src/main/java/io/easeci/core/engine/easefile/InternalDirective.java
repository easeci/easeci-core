package io.easeci.core.engine.easefile;

import io.easeci.extension.command.Command;
import io.easeci.extension.command.Directive;
import io.easeci.extension.directive.CodeChunk;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class InternalDirective implements Directive {
    private String directiveName;
    private List<Command> commandList;

    @Override
    public List<Command> getAvailableCommandList() throws IllegalAccessException {
        throw new IllegalAccessException("Method not implemented. Override it or not use.");
    }

    @Override
    public CodeChunk provideCode(List<Command> commands) throws IllegalAccessException {
        throw new IllegalAccessException("Method not implemented. Override it or not use.");
    }
}
