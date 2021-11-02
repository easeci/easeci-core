package io.easeci.core.engine.runtime;

import io.easeci.core.engine.runtime.commons.PipelineState;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Objects.isNull;

/**
 * A validator that checks if a given PipelineContext
 * is ready and valid to pass it to the Scheduler.
 * @author Karol Meksuła
 * 2021-10-29
 * */
public class PipelineContextReadinessValidator {

    static final String PIPELINE_CONTEXT_ID_NULL = "pipelineContextId cannot be null",
                            PIPELINE_ID_NULL = "pipelineId cannot be null",
                            LOG_BUFFER_NULL = "logBuffer cannot be null",
                            EXECUTABLE_SCRIPT_EMPTY = "executableScript cannot be empty",
                            START_TIMESTAMP_ZERO = "startTimestamp cannot equals zero",
                            PIPELINE_STATE_NOT_VALID = "pipelineState is null or not equals to WAITING_FOR_SCHEDULE"
                                    ;

    /**
     * READY_FOR_SCHEDULE when validation process was successful and PipelineContext is ready for schedule
     * VALIDATION_ERROR when validation process failed for some reason
     * Gathers errors on list and return
     * @return PipelineContextValidationResult is combination of PipelineStatus and error messages.
     * */
    public PipelineContextValidationResult validate(PipelineContext pipelineContext) {
        List<String> errorMessages = new ArrayList<>();

        valid(pipelineContext, pc -> isNull(pc.getPipelineContextId()), PIPELINE_CONTEXT_ID_NULL).ifPresent(errorMessages::add);
        valid(pipelineContext, pc -> isNull(pc.getPipelineId()), PIPELINE_ID_NULL).ifPresent(errorMessages::add);
        valid(pipelineContext, pc -> isNull(pc.getLogBuffer()), LOG_BUFFER_NULL).ifPresent(errorMessages::add);
        valid(pipelineContext, pc -> pc.getExecutableScript().isEmpty(), EXECUTABLE_SCRIPT_EMPTY).ifPresent(errorMessages::add);
        valid(pipelineContext, pc -> pc.getStartTimestamp() == 0L, START_TIMESTAMP_ZERO).ifPresent(errorMessages::add);
        valid(pipelineContext, pc -> isNull(pc.getPipelineState()) || !PipelineState.WAITING_FOR_SCHEDULE.equals(pc.getPipelineState()), PIPELINE_STATE_NOT_VALID).ifPresent(errorMessages::add);

        return errorMessages.isEmpty() ? PipelineContextValidationResult.of(PipelineState.READY_FOR_SCHEDULE, errorMessages)
                                       : PipelineContextValidationResult.of(PipelineState.VALIDATION_ERROR, errorMessages);
    }

    private Optional<String> valid(PipelineContext pipelineContext, Predicate<PipelineContext> predicate, String errorMessage) {
        if (predicate.test(pipelineContext)) {
            return Optional.of(errorMessage);
        }
        return Optional.empty();
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE, staticName = "of")
    static class PipelineContextValidationResult {
        private final PipelineState pipelineState;
        private final List<String> errorMessages; // messages to publish on logs of pipeline
    }
}
