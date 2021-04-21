package io.easeci.core.workspace.vars;

import java.util.Optional;

public interface GlobalVariablesFinder {

    Optional<Variable<?>> find(String variableName);
}
