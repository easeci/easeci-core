package io.easeci.core.workspace.vars;

import java.util.Map;
import java.util.Optional;

public interface GlobalVariables {

    <T> Optional<Variable<T>> get(String varName);

    <T> Variable<T> put(Variable<T> var) throws IllegalStateException;

    <T> Optional<Variable<T>> remove(String varName) throws IllegalStateException;

    <T> Variable<T> edit(Variable<T> var);

    int variableSize();

    Map<String, Variable<?>> getAllVariables();
}
