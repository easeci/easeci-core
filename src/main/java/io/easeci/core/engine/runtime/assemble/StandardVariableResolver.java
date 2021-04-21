package io.easeci.core.engine.runtime.assemble;

import io.easeci.core.engine.pipeline.EasefileObjectModel;
import io.easeci.core.engine.pipeline.Step;
import io.easeci.core.engine.runtime.PipelineRuntimeError;
import io.easeci.core.workspace.vars.GlobalVariablesFinder;
import io.easeci.core.workspace.vars.Variable;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;

@Slf4j
public class StandardVariableResolver extends VariableResolver {

    private static final Pattern VAR_MARKER = Pattern.compile("\\{\\{[a-zA-Z0-9_-]{1,130}}}");

    public StandardVariableResolver(EasefileObjectModel easefileObjectModel, GlobalVariablesFinder globalVariablesFinder) {
        super(easefileObjectModel, globalVariablesFinder);
    }

    @Override
    public EasefileObjectModel resolve() {
        easefileObjectModel.getStages()
                            .forEach(stage ->
                                        stage.getSteps()
                                                .forEach(step -> resolveStep(step, stage.getVariables())));

        return easefileObjectModel;
    }

    private Step resolveStep(Step step, List<Variable> stageVariables) {
        log.info("Variable resolving in step with order: {}", step.getOrder());
        step.setDirectiveName(resolve(step.getDirectiveName(), stageVariables));
        step.setInvocationBody(resolve(step.getInvocationBody(), stageVariables));
        return step;
    }

    /**
     * This method searches for variables in correct order:
     * - Global variables from system
     * - Variables from Easefile
     * - Variables from current stage
     * - if variable not exists - make and collects all that errors and return
     * */
    private String resolve(String value, List<Variable> stageVariables) {
        Matcher matcher = VAR_MARKER.matcher(value);
        while (matcher.find()) {
            String charSeqToRepl = matcher.group();
            String variableName = dropBraces(charSeqToRepl);
            Variable<?> variable = find(variableName, stageVariables);
            String variableAsString = variable.getValue().toString();
            value = value.replace(charSeqToRepl, variableAsString);
            log.info("Resolved variable with name: {}", variableName);
        }
        return value;
    }

    private Variable<?> find(String variableName, List<Variable> stageVariables) {
        return ofNullable(stageVariables).orElse(Collections.emptyList())
                .stream()
                .filter(variable -> variable.getName().equals(variableName))
                .findFirst()
                .orElseGet(() -> ofNullable(this.easefileObjectModel.getVariables()).orElse(Collections.emptyList())
                        .stream()
                        .filter(variable -> variable.getName().equals(variableName))
                        .findFirst()
                        .orElseGet(() -> globalVariablesFinder.find(variableName)
                                .orElseThrow(() -> {
                                    log.error("Cannot find variable named: {} in EaseCI and Easefile content", variableName);
                                    return new PipelineRuntimeError("Cannot find variable named: " + variableName);
                                })));
    }

    private String dropBraces(String value) {
        return value.substring(2, value.length() - 2);
    }
}
