package io.easeci.core.engine.runtime;

import io.easeci.core.engine.pipeline.EasefileObjectModel;
import io.easeci.core.engine.pipeline.Stage;
import io.easeci.core.engine.pipeline.Step;

import java.util.List;

public class Utils {

    static EasefileObjectModel provideEasefileObjectModel() {
        return EasefileObjectModel.builder()
                .stages(List.of(
                        // stage 0
                        Stage.builder()
                             .order(0)
                             .steps(List.of(
                                        new Step(0, "$mockedDirective", "mocked params - stage 0"),
                                        new Step(1, "$mockedDirective", "mocked params - stage 0"),
                                        new Step(2, "$mockedDirective", "mocked params - stage 0")
                                     )
                             )
                             .build(),
                        // stage 1
                        Stage.builder()
                             .order(1)
                             .steps(List.of(new Step(0, "$mockedDirective", "mocked params - stage 1")))
                             .build(),
                        // stage 2
                        Stage.builder()
                             .steps(List.of(
                                     new Step(0, "$mockedDirective", "mocked params - stage 2"),
                                     new Step(1, "$mockedDirective", "mocked params - stage 2")
                                     )
                             )
                             .order(2)
                             .build(),
                        // stage 3
                        Stage.builder()
                             .order(3)
                             .steps(List.of(
                                     new Step(0, "$mockedDirective", "mocked params - stage 3"),
                                     new Step(1, "$mockedDirective", "mocked params - stage 3"),
                                     new Step(2, "$mockedDirective", "mocked params - stage 3"),
                                     new Step(3, "$mockedDirective", "mocked params - stage 3"),
                                     new Step(4, "$mockedDirective", "mocked params - stage 3"),
                                     new Step(5, "$mockedDirective", "mocked params - stage 3"),
                                     new Step(6, "$mockedDirective", "mocked params - stage 3"),
                                     new Step(7, "$mockedDirective", "mocked params - stage 3")
                                     )
                             )
                             .build()
                ))
                .build();
    }
}
