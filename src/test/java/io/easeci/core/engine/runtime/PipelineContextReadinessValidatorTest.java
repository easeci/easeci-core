package io.easeci.core.engine.runtime;

import io.easeci.BaseWorkspaceContextTest;
import io.easeci.core.engine.runtime.assemble.PerformerTaskDistributor;
import io.easeci.core.engine.runtime.assemble.PythonScriptAssembler;
import io.easeci.core.engine.runtime.assemble.ScriptAssembler;
import io.easeci.core.engine.runtime.assemble.StandardPerformerTaskDistributor;
import io.easeci.core.engine.runtime.commons.PipelineState;
import io.easeci.core.engine.runtime.logs.LogBuffer;
import io.easeci.core.workspace.projects.PipelineIO;
import io.easeci.core.workspace.vars.GlobalVariablesFinder;
import io.easeci.core.workspace.vars.GlobalVariablesManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static io.easeci.core.engine.runtime.PipelineContextReadinessValidator.*;
import static org.junit.jupiter.api.Assertions.*;

class PipelineContextReadinessValidatorTest extends BaseWorkspaceContextTest {

    @Test
    @DisplayName("Should successfully validate PipelineContext and return no errors")
    void successTest() throws PipelineNotExists, NoSuchFieldException, IllegalAccessException {
        PipelineContextReadinessValidator readinessValidator = new PipelineContextReadinessValidator();

        PipelineContext pipelineContext = factorize();

        PipelineContextReadinessValidator.PipelineContextValidationResult validationResult = readinessValidator.validate(pipelineContext);

        assertAll(() -> assertTrue(validationResult.getErrorMessages().isEmpty()));
    }

    @ParameterizedTest
    @DisplayName("Should add correct error message to list")
    @MethodSource("providePipelineContextWithErrorMessages")
    void errorMessageTest(PipelineContext pipelineContext, List<String> errorMessagesExpected) {
        PipelineContextReadinessValidator readinessValidator = new PipelineContextReadinessValidator();

        PipelineContextReadinessValidator.PipelineContextValidationResult validationResult = readinessValidator.validate(pipelineContext);

        assertAll(() -> assertEquals(1, validationResult.getErrorMessages().size()),
                  () -> assertEquals(errorMessagesExpected.get(0), validationResult.getErrorMessages().get(0)));
    }

    private static Stream<Arguments> providePipelineContextWithErrorMessages() throws PipelineNotExists, NoSuchFieldException, IllegalAccessException {
        return Stream.of(
                pipelineContextIdNull(),
                pipelineIdNull(),
                logBufferNull(),
                executableScriptEmpty(),
                startTimestampNull(),
                pipelineStateNotValid()
        );
    }

    @Test
    @DisplayName("Should receive more than one error message but in correct order")
    void multipleErrorMessageTest() throws PipelineNotExists, NoSuchFieldException, IllegalAccessException {
        PipelineContextReadinessValidator readinessValidator = new PipelineContextReadinessValidator();

        PipelineContext pipelineContext = factorizeWithErrors();

        PipelineContextReadinessValidator.PipelineContextValidationResult validationResult = readinessValidator.validate(pipelineContext);

        assertAll(() -> assertFalse(validationResult.getErrorMessages().isEmpty()),
                () -> assertEquals(START_TIMESTAMP_ZERO, validationResult.getErrorMessages().get(0)),
                () -> assertEquals(PIPELINE_STATE_NOT_VALID, validationResult.getErrorMessages().get(1))
        );
    }

    private PipelineContext factorize() throws PipelineNotExists, NoSuchFieldException, IllegalAccessException {
        final PipelineContextFactory pipelineContextFactory = Mockito.mock(PipelineContextFactory.class);
        final PipelineContextSystem pipelineContextSystem = PipelineContextSystem.getInstance(pipelineContextFactory);
        UUID pipelineContextId = UUID.randomUUID();
        UUID pipelineId = UUID.randomUUID();

        PipelineIO pipelineIOMock = Mockito.mock(PipelineIO.class);
        LogBuffer logBuffer = Mockito.mock(LogBuffer.class);
        GlobalVariablesFinder globalVariablesFinder = Mockito.mock(GlobalVariablesManager.class);
        PerformerTaskDistributor performerTaskDistributor = Mockito.mock(StandardPerformerTaskDistributor.class);
        ScriptAssembler scriptAssembler = new PythonScriptAssembler();

        PipelineContext pipelineContext = new PipelineContext(pipelineId, pipelineContextId, pipelineContextSystem, performerTaskDistributor, globalVariablesFinder, scriptAssembler, pipelineIOMock, logBuffer);

        Field scriptAssembled = pipelineContext.getClass().getDeclaredField("scriptAssembled");
        scriptAssembled.setAccessible(true);
        scriptAssembled.set(pipelineContext, "#!/bin/bash\necho 'Hello world!'");

        Field startTimestamp = pipelineContext.getClass().getDeclaredField("startTimestamp");
        startTimestamp.setAccessible(true);
        startTimestamp.setLong(pipelineContext, System.currentTimeMillis());

        Field pipelineState = pipelineContext.getClass().getDeclaredField("pipelineState");
        pipelineState.setAccessible(true);
        pipelineState.set(pipelineContext, PipelineState.WAITING_FOR_SCHEDULE);

        return pipelineContext;
    }

    private PipelineContext factorizeWithErrors() throws PipelineNotExists, NoSuchFieldException, IllegalAccessException {
        final PipelineContextFactory pipelineContextFactory = Mockito.mock(PipelineContextFactory.class);
        final PipelineContextSystem pipelineContextSystem = PipelineContextSystem.getInstance(pipelineContextFactory);
        UUID pipelineContextId = UUID.randomUUID();
        UUID pipelineId = UUID.randomUUID();

        PipelineIO pipelineIOMock = Mockito.mock(PipelineIO.class);
        LogBuffer logBuffer = Mockito.mock(LogBuffer.class);
        GlobalVariablesFinder globalVariablesFinder = Mockito.mock(GlobalVariablesManager.class);
        PerformerTaskDistributor performerTaskDistributor = Mockito.mock(StandardPerformerTaskDistributor.class);
        ScriptAssembler scriptAssembler = new PythonScriptAssembler();

        PipelineContext pipelineContext = new PipelineContext(pipelineId, pipelineContextId, pipelineContextSystem, performerTaskDistributor, globalVariablesFinder, scriptAssembler, pipelineIOMock, logBuffer);

        Field scriptAssembled = pipelineContext.getClass().getDeclaredField("scriptAssembled");
        scriptAssembled.setAccessible(true);
        scriptAssembled.set(pipelineContext, "#!/bin/bash\necho 'Hello world!'");

        return pipelineContext;
    }

    private static Arguments pipelineContextIdNull() throws PipelineNotExists, NoSuchFieldException, IllegalAccessException {
        final PipelineContextFactory pipelineContextFactory = Mockito.mock(PipelineContextFactory.class);
        final PipelineContextSystem pipelineContextSystem = PipelineContextSystem.getInstance(pipelineContextFactory);
        UUID pipelineContextId = null;
        UUID pipelineId = UUID.randomUUID();

        PipelineIO pipelineIOMock = Mockito.mock(PipelineIO.class);
        LogBuffer logBuffer = Mockito.mock(LogBuffer.class);
        GlobalVariablesFinder globalVariablesFinder = Mockito.mock(GlobalVariablesManager.class);
        PerformerTaskDistributor performerTaskDistributor = Mockito.mock(StandardPerformerTaskDistributor.class);
        ScriptAssembler scriptAssembler = new PythonScriptAssembler();

        PipelineContext pipelineContext = new PipelineContext(pipelineId, pipelineContextId, pipelineContextSystem, performerTaskDistributor, globalVariablesFinder, scriptAssembler, pipelineIOMock, logBuffer);

        Field scriptAssembled = pipelineContext.getClass().getDeclaredField("scriptAssembled");
        scriptAssembled.setAccessible(true);
        scriptAssembled.set(pipelineContext, "#!/bin/bash\necho 'Hello world!'");

        Field startTimestamp = pipelineContext.getClass().getDeclaredField("startTimestamp");
        startTimestamp.setAccessible(true);
        startTimestamp.setLong(pipelineContext, System.currentTimeMillis());

        Field pipelineState = pipelineContext.getClass().getDeclaredField("pipelineState");
        pipelineState.setAccessible(true);
        pipelineState.set(pipelineContext, PipelineState.WAITING_FOR_SCHEDULE);

        return Arguments.of(
                pipelineContext, List.of(PIPELINE_CONTEXT_ID_NULL)
        );
    }

    private static Arguments pipelineIdNull() throws PipelineNotExists, NoSuchFieldException, IllegalAccessException {
        final PipelineContextFactory pipelineContextFactory = Mockito.mock(PipelineContextFactory.class);
        final PipelineContextSystem pipelineContextSystem = PipelineContextSystem.getInstance(pipelineContextFactory);
        UUID pipelineContextId = UUID.randomUUID();
        UUID pipelineId = null;

        PipelineIO pipelineIOMock = Mockito.mock(PipelineIO.class);
        LogBuffer logBuffer = Mockito.mock(LogBuffer.class);
        GlobalVariablesFinder globalVariablesFinder = Mockito.mock(GlobalVariablesManager.class);
        PerformerTaskDistributor performerTaskDistributor = Mockito.mock(StandardPerformerTaskDistributor.class);
        ScriptAssembler scriptAssembler = new PythonScriptAssembler();

        PipelineContext pipelineContext = new PipelineContext(pipelineId, pipelineContextId, pipelineContextSystem, performerTaskDistributor, globalVariablesFinder, scriptAssembler, pipelineIOMock, logBuffer);

        Field scriptAssembled = pipelineContext.getClass().getDeclaredField("scriptAssembled");
        scriptAssembled.setAccessible(true);
        scriptAssembled.set(pipelineContext, "#!/bin/bash\necho 'Hello world!'");

        Field startTimestamp = pipelineContext.getClass().getDeclaredField("startTimestamp");
        startTimestamp.setAccessible(true);
        startTimestamp.setLong(pipelineContext, System.currentTimeMillis());

        Field pipelineState = pipelineContext.getClass().getDeclaredField("pipelineState");
        pipelineState.setAccessible(true);
        pipelineState.set(pipelineContext, PipelineState.WAITING_FOR_SCHEDULE);

        return Arguments.of(
                pipelineContext, List.of(PIPELINE_ID_NULL)
        );
    }

    private static Arguments logBufferNull() throws PipelineNotExists, NoSuchFieldException, IllegalAccessException {
        final PipelineContextFactory pipelineContextFactory = Mockito.mock(PipelineContextFactory.class);
        final PipelineContextSystem pipelineContextSystem = PipelineContextSystem.getInstance(pipelineContextFactory);
        UUID pipelineContextId = UUID.randomUUID();
        UUID pipelineId = UUID.randomUUID();

        PipelineIO pipelineIOMock = Mockito.mock(PipelineIO.class);
        LogBuffer logBuffer = null;
        GlobalVariablesFinder globalVariablesFinder = Mockito.mock(GlobalVariablesManager.class);
        PerformerTaskDistributor performerTaskDistributor = Mockito.mock(StandardPerformerTaskDistributor.class);
        ScriptAssembler scriptAssembler = new PythonScriptAssembler();

        PipelineContext pipelineContext = new PipelineContext(pipelineId, pipelineContextId, pipelineContextSystem, performerTaskDistributor, globalVariablesFinder, scriptAssembler, pipelineIOMock, logBuffer);

        Field scriptAssembled = pipelineContext.getClass().getDeclaredField("scriptAssembled");
        scriptAssembled.setAccessible(true);
        scriptAssembled.set(pipelineContext, "#!/bin/bash\necho 'Hello world!'");

        Field startTimestamp = pipelineContext.getClass().getDeclaredField("startTimestamp");
        startTimestamp.setAccessible(true);
        startTimestamp.setLong(pipelineContext, System.currentTimeMillis());

        Field pipelineState = pipelineContext.getClass().getDeclaredField("pipelineState");
        pipelineState.setAccessible(true);
        pipelineState.set(pipelineContext, PipelineState.WAITING_FOR_SCHEDULE);

        return Arguments.of(
                pipelineContext, List.of(LOG_BUFFER_NULL)
        );
    }

    private static Arguments executableScriptEmpty() throws PipelineNotExists, NoSuchFieldException, IllegalAccessException {
        final PipelineContextFactory pipelineContextFactory = Mockito.mock(PipelineContextFactory.class);
        final PipelineContextSystem pipelineContextSystem = PipelineContextSystem.getInstance(pipelineContextFactory);
        UUID pipelineContextId = UUID.randomUUID();
        UUID pipelineId = UUID.randomUUID();

        PipelineIO pipelineIOMock = Mockito.mock(PipelineIO.class);
        LogBuffer logBuffer = Mockito.mock(LogBuffer.class);
        GlobalVariablesFinder globalVariablesFinder = Mockito.mock(GlobalVariablesManager.class);
        PerformerTaskDistributor performerTaskDistributor = Mockito.mock(StandardPerformerTaskDistributor.class);
        ScriptAssembler scriptAssembler = new PythonScriptAssembler();

        PipelineContext pipelineContext = new PipelineContext(pipelineId, pipelineContextId, pipelineContextSystem, performerTaskDistributor, globalVariablesFinder, scriptAssembler, pipelineIOMock, logBuffer);

        Field scriptAssembled = pipelineContext.getClass().getDeclaredField("scriptAssembled");
        scriptAssembled.setAccessible(true);
        scriptAssembled.set(pipelineContext, "");

        Field startTimestamp = pipelineContext.getClass().getDeclaredField("startTimestamp");
        startTimestamp.setAccessible(true);
        startTimestamp.setLong(pipelineContext, System.currentTimeMillis());

        Field pipelineState = pipelineContext.getClass().getDeclaredField("pipelineState");
        pipelineState.setAccessible(true);
        pipelineState.set(pipelineContext, PipelineState.WAITING_FOR_SCHEDULE);

        return Arguments.of(
                pipelineContext, List.of(EXECUTABLE_SCRIPT_EMPTY)
        );
    }

    private static Arguments startTimestampNull() throws PipelineNotExists, NoSuchFieldException, IllegalAccessException {
        final PipelineContextFactory pipelineContextFactory = Mockito.mock(PipelineContextFactory.class);
        final PipelineContextSystem pipelineContextSystem = PipelineContextSystem.getInstance(pipelineContextFactory);
        UUID pipelineContextId = UUID.randomUUID();
        UUID pipelineId = UUID.randomUUID();

        PipelineIO pipelineIOMock = Mockito.mock(PipelineIO.class);
        LogBuffer logBuffer = Mockito.mock(LogBuffer.class);
        GlobalVariablesFinder globalVariablesFinder = Mockito.mock(GlobalVariablesManager.class);
        PerformerTaskDistributor performerTaskDistributor = Mockito.mock(StandardPerformerTaskDistributor.class);
        ScriptAssembler scriptAssembler = new PythonScriptAssembler();

        PipelineContext pipelineContext = new PipelineContext(pipelineId, pipelineContextId, pipelineContextSystem, performerTaskDistributor, globalVariablesFinder, scriptAssembler, pipelineIOMock, logBuffer);

        Field scriptAssembled = pipelineContext.getClass().getDeclaredField("scriptAssembled");
        scriptAssembled.setAccessible(true);
        scriptAssembled.set(pipelineContext, "#!/bin/bash\necho 'Hello world!'");

        Field startTimestamp = pipelineContext.getClass().getDeclaredField("startTimestamp");
        startTimestamp.setAccessible(true);
        startTimestamp.setLong(pipelineContext, 0L);


        Field pipelineState = pipelineContext.getClass().getDeclaredField("pipelineState");
        pipelineState.setAccessible(true);
        pipelineState.set(pipelineContext, PipelineState.WAITING_FOR_SCHEDULE);

        return Arguments.of(
                pipelineContext, List.of(START_TIMESTAMP_ZERO)
        );
    }

    private static Arguments pipelineStateNotValid() throws PipelineNotExists, NoSuchFieldException, IllegalAccessException {
        final PipelineContextFactory pipelineContextFactory = Mockito.mock(PipelineContextFactory.class);
        final PipelineContextSystem pipelineContextSystem = PipelineContextSystem.getInstance(pipelineContextFactory);
        UUID pipelineContextId = UUID.randomUUID();
        UUID pipelineId = UUID.randomUUID();

        PipelineIO pipelineIOMock = Mockito.mock(PipelineIO.class);
        LogBuffer logBuffer = Mockito.mock(LogBuffer.class);
        GlobalVariablesFinder globalVariablesFinder = Mockito.mock(GlobalVariablesManager.class);
        PerformerTaskDistributor performerTaskDistributor = Mockito.mock(StandardPerformerTaskDistributor.class);
        ScriptAssembler scriptAssembler = new PythonScriptAssembler();

        PipelineContext pipelineContext = new PipelineContext(pipelineId, pipelineContextId, pipelineContextSystem, performerTaskDistributor, globalVariablesFinder, scriptAssembler, pipelineIOMock, logBuffer);

        Field scriptAssembled = pipelineContext.getClass().getDeclaredField("scriptAssembled");
        scriptAssembled.setAccessible(true);
        scriptAssembled.set(pipelineContext, "#!/bin/bash\necho 'Hello world!'");

        Field startTimestamp = pipelineContext.getClass().getDeclaredField("startTimestamp");
        startTimestamp.setAccessible(true);
        startTimestamp.setLong(pipelineContext, System.currentTimeMillis());

        Field pipelineState = pipelineContext.getClass().getDeclaredField("pipelineState");
        pipelineState.setAccessible(true);
        pipelineState.set(pipelineContext, PipelineState.ABORTED_PREPARATION_ERROR);

        return Arguments.of(
                pipelineContext, List.of(PIPELINE_STATE_NOT_VALID)
        );
    }
}