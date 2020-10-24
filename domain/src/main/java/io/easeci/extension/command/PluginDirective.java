package io.easeci.extension.command;

import io.easeci.extension.directive.CodeChunk;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PluginDirective implements Directive {
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
