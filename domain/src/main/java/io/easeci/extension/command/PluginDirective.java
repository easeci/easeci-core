package io.easeci.extension.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PluginDirective implements Directive {
    private String directiveName;
    private List<Command> commandList;
}
