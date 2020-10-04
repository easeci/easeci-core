package io.easeci.extension.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommandParameter {
    private String parameterName;
    private String parameterValue;
    private VariableType parameterType;
}
