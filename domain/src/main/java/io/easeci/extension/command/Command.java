package io.easeci.extension.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class Command {
    private String commandName;
    private List<CommandParameter> commandParameterList;
}
