package io.easeci.core.workspace.vars;

import com.google.common.collect.ImmutableMap;
import io.easeci.core.workspace.SerializeUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelName.WORKSPACE_EVENT;
import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelPrefix.THREE;
import static io.easeci.core.log.ApplicationLevelLogFacade.logit;
import static io.easeci.core.workspace.LocationUtils.getVarsFileLocation;
import static java.util.Objects.isNull;

public class GlobalVariablesManager implements GlobalVariables, GlobalVariablesFinder {
    private static GlobalVariablesManager instance;
    private static Map<String, Variable<?>> varsMap;
    private static Path varsFile;

    private GlobalVariablesManager() {}

    public static GlobalVariablesManager getInstance() {
        if (instance == null) {
            instance = new GlobalVariablesManager();
            varsMap = new HashMap<>();
            varsFile = getVarsFileLocation();
            initializeVarsFile();
        }
        return instance;
    }

    private static void initializeVarsFile() {
        boolean isFileExists = Files.exists(varsFile);
        if (isFileExists) {
            logit(WORKSPACE_EVENT, "File for store variables found in here: " + varsFile.toString(), THREE);
        }
        try {
            Files.createFile(varsFile);
            logit(WORKSPACE_EVENT, "Created file with success for store variables in here: " + varsFile.toString(), THREE);
            instance.save();
            logit(WORKSPACE_EVENT, "Created file with success for store variables in here: " + varsFile.toString(), THREE);
        } catch (IOException e) {
            e.printStackTrace();
            logit(WORKSPACE_EVENT, "Exception occurred and could not create file for store variables in here: " + varsFile.toString(), THREE);
        }
    }

    @Override
    public <T> Optional<Variable<T>> get(String varName) {
        Variable<T> variable = (Variable<T>) varsMap.get(varName);
        return Optional.ofNullable(variable);
    }

    @Override
    public <T> Variable<T> put(Variable<T> var) throws IllegalStateException {
        if (isNull(var) || isNull(var.getName()) || isNull(var.getType()) || isNull(var.getValue())) {
            logit(WORKSPACE_EVENT, "Variable object not initialized correctly.");
            throw new IllegalStateException("Variable object not initialized correctly.");
        }
        if (varsMap.containsKey(var.getName())) {
            logit(WORKSPACE_EVENT, "Variable exists. Cannot add twice the same name of variable.");
            throw new IllegalStateException("Variable exists. Cannot add twice the same name of variable.");
        }
        validateVarName(var.getName());
        varsMap.put(var.getName(), var);
        save();
        return var;
    }

    @Override
    public <T> Optional<Variable<T>> remove(String varName) {
        Optional<? extends Variable<?>> found = varsMap.keySet().stream()
                .filter(key -> key.equals(varName))
                .map(key -> varsMap.get(key))
                .findFirst();
        if (found.isPresent()) {
            Variable<?> variable = found.get();
            varsMap.remove(variable.getName(), variable);
            save();
        }
        return (Optional<Variable<T>>) found;
    }

    @Override
    public <T> Variable<T> edit(Variable<T> var) {
        validateVarName(var.getName());
        if (varsMap.containsKey(var.getName())) {
            varsMap.put(var.getName(), var);
            return var;
        }
        throw new IllegalStateException("Variable with name: " + var.getName() + " not exists.");
    }

    @Override
    public int variableSize() {
        return varsMap.size();
    }

    @Override
    public Map<String, Variable<?>> getAllVariables() {
        return ImmutableMap.<String, Variable<?>>builder()
                .putAll(varsMap)
                .build();
    }

    @Override
    public Optional<Variable<?>> find(String variableName) {
        return Optional.ofNullable(varsMap.get(variableName));
    }

    private void save() {
        Path varsFile = getVarsFileLocation();
        byte[] write = SerializeUtils.write(varsMap);
        try {
            Files.write(varsFile, write);
        } catch (IOException e) {
            e.printStackTrace();
            logit(WORKSPACE_EVENT, "Exception occurred and could not save to file store variables. File should be in here: " + varsFile.toString(), THREE);
        }
    }

    private void validateVarName(String variableName) {
        Pattern pattern = Pattern.compile("^((?=[A-Za-z0-9])(?![_\\-]).)*${1,20}");
        Matcher matcher = pattern.matcher(variableName);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Variable name: '" + variableName + "' is not correct!");
        }
    }

    void clear() {
        varsMap.clear();
    }
}
