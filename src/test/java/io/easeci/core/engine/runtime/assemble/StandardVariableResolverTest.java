package io.easeci.core.engine.runtime.assemble;

import io.easeci.BaseWorkspaceContextTest;
import io.easeci.core.engine.pipeline.EasefileObjectModel;
import io.easeci.core.engine.pipeline.Step;
import io.easeci.core.workspace.vars.GlobalVariablesFinder;
import io.easeci.core.workspace.vars.GlobalVariablesManager;
import io.easeci.core.workspace.vars.Variable;
import io.easeci.extension.command.VariableType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.easeci.core.engine.runtime.assemble.Utils.provideEasefileObjectModelWithVars;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StandardVariableResolverTest extends BaseWorkspaceContextTest {

    private static final GlobalVariablesFinder globalVariablesFinder = GlobalVariablesManager.getInstance();

    @Test
    @DisplayName("Trial test to find out how to write mechanism")
    void trialTest() {
        EasefileObjectModel easefileObjectModel = provideEasefileObjectModelWithVars();

        GlobalVariablesManager instance = GlobalVariablesManager.getInstance();
        instance.put(Variable.of(VariableType.STRING, "version", "2.0.1"));
        instance.put(Variable.of(VariableType.STRING, "source", "origin"));
        instance.put(Variable.of(VariableType.STRING, "branch", "master"));
        instance.put(Variable.of(VariableType.STRING, "strategy", "--rebase"));

        VariableResolver variableResolver = new StandardVariableResolver(easefileObjectModel, globalVariablesFinder);

        EasefileObjectModel resolvedEom = variableResolver.resolve();

        Step step = resolvedEom.getStages().get(3).getSteps().get(2);
        final String directiveName = step.getDirectiveName();
        final String invocationBody = step.getInvocationBody();

        assertAll(() -> assertEquals(directiveName, "$git"),
                  () -> assertEquals(invocationBody, "pull origin master --rebase"));
    }

//    1. Should read variable from local stage declaration
//    2. Should read variable from Easefile declaration
//    3. Should read variable from Global storage declaration

//    4. Variable not found
//    5. More than one variables not found - (collect exception and return it in response)
//    6. Broken braces
}