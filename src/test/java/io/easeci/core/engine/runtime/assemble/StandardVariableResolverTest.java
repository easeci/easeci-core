package io.easeci.core.engine.runtime.assemble;

import io.easeci.BaseWorkspaceContextTest;
import io.easeci.core.engine.pipeline.EasefileObjectModel;
import io.easeci.core.engine.pipeline.Step;
import io.easeci.core.engine.runtime.VariableResolveException;
import io.easeci.core.workspace.vars.GlobalVariablesFinder;
import io.easeci.core.workspace.vars.GlobalVariablesManager;
import io.easeci.core.workspace.vars.Variable;
import io.easeci.extension.command.VariableType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static io.easeci.core.engine.runtime.assemble.Utils.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StandardVariableResolverTest extends BaseWorkspaceContextTest {

    private static final GlobalVariablesFinder globalVariablesFinder = GlobalVariablesManager.getInstance();

    @BeforeEach
    void setupBeforeEach() {
        GlobalVariablesManager.getInstance().clear();
    }

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

        EasefileObjectModel resolvedEom = null;
        try {
            resolvedEom = variableResolver.resolve();
        } catch (VariableResolveException e) {
            e.printStackTrace();
        }

        assert resolvedEom != null;
        Step step = resolvedEom.getStages().get(3).getSteps().get(2);
        final String directiveName = step.getDirectiveName();
        final String invocationBody = step.getInvocationBody();

        assertAll(() -> assertEquals(directiveName, "$git"),
                  () -> assertEquals(invocationBody, "pull origin master --rebase"));
    }

    @Test
    @DisplayName("Should read variable from local stage declaration")
    void localStageVariableTest() {
        EasefileObjectModel easefileObjectModel = provideEasefileObjectModelVariableStageDeclaration();

        VariableResolver variableResolver = new StandardVariableResolver(easefileObjectModel, globalVariablesFinder);

        EasefileObjectModel resolvedEom = null;
        try {
            resolvedEom = variableResolver.resolve();
        } catch (VariableResolveException e) {
            e.printStackTrace();
        }

        assert resolvedEom != null;
        Step step = resolvedEom.getStages().get(0).getSteps().get(0);
        final String directiveName = step.getDirectiveName();
        final String invocationBody = step.getInvocationBody();

        assertAll(() -> assertEquals(directiveName, "$git"),
                () -> assertEquals(invocationBody, "checkout stage"));
    }

    @Test
    @DisplayName("Should read variable from Easefile declaration")
    void easefileVariableTest() {
        EasefileObjectModel easefileObjectModel = provideEasefileObjectModelVariableEasefileDeclaration();

        VariableResolver variableResolver = new StandardVariableResolver(easefileObjectModel, globalVariablesFinder);

        EasefileObjectModel resolvedEom = null;
        try {
            resolvedEom = variableResolver.resolve();
        } catch (VariableResolveException e) {
            e.printStackTrace();
        }

        assert resolvedEom != null;
        Step step = resolvedEom.getStages().get(0).getSteps().get(0);
        final String directiveName = step.getDirectiveName();
        final String invocationBody = step.getInvocationBody();

        assertAll(() -> assertEquals(directiveName, "$git"),
                () -> assertEquals(invocationBody, "checkout develop"));
    }

    @Test
    @DisplayName("Should read variable from Global storage declaration")
    void globalVariableTest() {
        EasefileObjectModel easefileObjectModel = provideEasefileObjectModelVariableNoVariableDeclaration();

        GlobalVariablesManager instance = GlobalVariablesManager.getInstance();
        // declaring global variable
        instance.put(Variable.of(VariableType.STRING, "branch", "master"));

        VariableResolver variableResolver = new StandardVariableResolver(easefileObjectModel, globalVariablesFinder);

        EasefileObjectModel resolvedEom = null;
        try {
            resolvedEom = variableResolver.resolve();
        } catch (VariableResolveException e) {
            e.printStackTrace();
        }

        assert resolvedEom != null;
        Step step = resolvedEom.getStages().get(0).getSteps().get(0);
        final String directiveName = step.getDirectiveName();
        final String invocationBody = step.getInvocationBody();

        assertAll(() -> assertEquals(directiveName, "$git"),
                () -> assertEquals(invocationBody, "checkout master"));
    }

    @Test
    @DisplayName("Should return few runtime exceptions with warning that more than one variables not exists")
    void variablesNotFoundTest() {
        EasefileObjectModel easefileObjectModel = provideEasefileObjectModelWithVars();

        VariableResolver variableResolver = new StandardVariableResolver(easefileObjectModel, globalVariablesFinder);

        List<VariableResolveException> pipelineRuntimeExceptions = new ArrayList<>();
        try {
            variableResolver.resolve();
        } catch (VariableResolveException e) {
            e.printStackTrace();
            pipelineRuntimeExceptions.addAll(e.getExceptionList());
        }

        assertAll(() -> assertEquals(4, pipelineRuntimeExceptions.size()));
    }

    @Test
    @DisplayName("Should not resolve variable when braces are not closed correctly")
    void bracesDeclarationBrokenTest() throws VariableResolveException {
        EasefileObjectModel easefileObjectModel = provideEasefileObjectModelBrokenBraces();

        VariableResolver variableResolver = new StandardVariableResolver(easefileObjectModel, globalVariablesFinder);

        EasefileObjectModel eomResolved = variableResolver.resolve();

        // not resolve if braces are not closed correctly (missing one closing brace)
        assertEquals("{{variable} params - stage 0", eomResolved.getStages().get(0).getSteps().get(0).getInvocationBody());
    }

    @Test
    @DisplayName("Should read variables in correct order 1.local stage 2. Easefile 3. Global")
    void variablesOrderTest() {
        // easefile with declared variables
        EasefileObjectModel easefileObjectModel = provideEasefileObjectModelVariableEasefileAndStage();

        GlobalVariablesManager instance = GlobalVariablesManager.getInstance();
        // declaring global variable
        instance.put(Variable.of(VariableType.STRING, "branch", "global"));

        VariableResolver variableResolver = new StandardVariableResolver(easefileObjectModel, globalVariablesFinder);

        EasefileObjectModel resolvedEom = null;
        try {
            resolvedEom = variableResolver.resolve();
        } catch (VariableResolveException e) {
            e.printStackTrace();
        }

        assert resolvedEom != null;
        Step step = resolvedEom.getStages().get(0).getSteps().get(0);
        final String directiveName = step.getDirectiveName();
        final String invocationBody = step.getInvocationBody();

        assertAll(() -> assertEquals(directiveName, "$git"),
                () -> assertEquals(invocationBody, "checkout easefile"));
    }

}