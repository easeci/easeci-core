package io.easeci.core.workspace.vars;

import java.util.Optional;

/**
 * Functional interface to searching for variables in global storage.
 * @author Karol Meksuła
 * 2021-04-24
 * */
public interface GlobalVariablesFinder {

    /**
     * Use this method to find variable in global storage by variable's name.
     * @param variableName name of variable, for instance: current_version
     * @return optional value with Variable<?> object.
     *          Optional will be empty if variable not exists in global storage.
     * */
    Optional<Variable<?>> find(String variableName);
}
