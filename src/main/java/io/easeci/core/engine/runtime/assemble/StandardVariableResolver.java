package io.easeci.core.engine.runtime.assemble;

import io.easeci.core.engine.pipeline.EasefileObjectModel;
import io.easeci.core.engine.pipeline.Step;
import io.easeci.core.engine.runtime.VariableResolveException;
import io.easeci.core.workspace.vars.GlobalVariablesFinder;
import io.easeci.core.workspace.vars.Variable;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.easeci.core.engine.runtime.RuntimeCommunicateType.ERROR;
import static java.util.Optional.ofNullable;

@Slf4j
public class StandardVariableResolver extends VariableResolver {

    private static final Pattern VAR_MARKER = Pattern.compile("\\{\\{[a-zA-Z0-9_-]{1,130}}}");

    public StandardVariableResolver(EasefileObjectModel easefileObjectModel, GlobalVariablesFinder globalVariablesFinder) {
        super(easefileObjectModel, globalVariablesFinder);
    }

    @Override
    public EasefileObjectModel resolve() throws VariableResolveException {
        final List<VariableResolveException> runtimeExceptions = new ArrayList<>(0);
        easefileObjectModel.getStages()
                            .forEach(stage ->
                                        stage.getSteps()
                                                .forEach(step -> {
                                                    Tuple2<Step, List<VariableResolveException>> stepListTuple2 = resolveStep(step, stage.getVariables());
                                                    if (!stepListTuple2._2.isEmpty()) {
                                                        runtimeExceptions.addAll(stepListTuple2._2);
                                                    }
                                                }));
        if (runtimeExceptions.isEmpty()) return easefileObjectModel;
        else throw new VariableResolveException(runtimeExceptions, ERROR);
    }

    private Tuple2<Step, List<VariableResolveException>> resolveStep(Step step, List<Variable> stageVariables) {
        log.info("Variable resolving in step with order: {}", step.getOrder());
        Tuple2<String, List<VariableResolveException>> directiveName = resolve(step.getDirectiveName(), stageVariables);
        Tuple2<String, List<VariableResolveException>> invocationBody = resolve(step.getInvocationBody(), stageVariables);
        step.setDirectiveName(directiveName._1);
        step.setInvocationBody(invocationBody._1);
        if (!directiveName._2.isEmpty() || !invocationBody._2.isEmpty()) {
            List<VariableResolveException> pipelineRuntimeExceptions = new ArrayList<>();
            pipelineRuntimeExceptions.addAll(directiveName._2);
            pipelineRuntimeExceptions.addAll(invocationBody._2);
            return Tuple.of(step, pipelineRuntimeExceptions) ;
        }
        return Tuple.of(step, Collections.emptyList()) ;
    }

    /**
     * This method searches for variables in correct order:
     * - Global variables from system
     * - Variables from Easefile
     * - Variables from current stage
     * - if variable not exists - make and collects all that errors and return
     * */
    private Tuple2<String, List<VariableResolveException>> resolve(String value, List<Variable> stageVariables) {
        Matcher matcher = VAR_MARKER.matcher(value);
        List<VariableResolveException> pipelineRuntimeExceptions = new ArrayList<>(0);
        while (matcher.find()) {
            String charSeqToRepl = matcher.group();
            String variableName = dropBraces(charSeqToRepl);
            Variable<?> variable = find(variableName, stageVariables);
            if (variable == null) {
                pipelineRuntimeExceptions.add(new VariableResolveException("Cannot find variable named: " + variableName, ERROR));
                continue;
            }
            String variableAsString = variable.getValue().toString();
            value = value.replace(charSeqToRepl, variableAsString);
            log.info("Resolved variable with name: {}", variableName);
        }
        return Tuple.of(value, pipelineRuntimeExceptions);
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
                                .orElse(null)));
    }

    private String dropBraces(String value) {
        return value.substring(2, value.length() - 2);
    }
}
