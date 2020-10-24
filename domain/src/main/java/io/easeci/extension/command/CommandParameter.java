package io.easeci.extension.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class CommandParameter {
    private String parameterName;
    private String parameterValue;
    private VariableType parameterType;

    private CommandParameter() { }

    public static CommandParameter of(VariableType parameterType, String parameterName) {
        CommandParameter commandParameter = new CommandParameter();
        commandParameter.parameterType = parameterType;
        commandParameter.parameterName = parameterName;
        return commandParameter;
    }
}
