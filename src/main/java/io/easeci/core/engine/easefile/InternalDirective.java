package io.easeci.core.engine.easefile;

import io.easeci.extension.command.Command;
import io.easeci.extension.command.Directive;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class InternalDirective implements Directive {
    private String directiveName;
    private List<Command> commandList;
}
