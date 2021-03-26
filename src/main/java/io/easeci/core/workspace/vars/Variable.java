package io.easeci.core.workspace.vars;

import io.easeci.extension.command.VariableType;
import lombok.*;

import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Variable<T> {
    private VariableType type;
    private String name;
    private T value;

    public static <T> Variable<T> of(VariableType type, String name, T value) {
        Variable<T> var = new Variable<>();
        var.type = type;
        var.name = name.trim();
        var.value = value;
        return var;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Variable<?> variable = (Variable<?>) o;
        return type == variable.type && Objects.equals(name, variable.name) && Objects.equals(value, variable.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, value);
    }
}
