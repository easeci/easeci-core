package io.easeci.core.engine.runtime.assemble;

import io.easeci.core.engine.pipeline.EasefileObjectModel;
import io.easeci.core.engine.pipeline.Stage;
import io.easeci.core.engine.pipeline.Step;
import io.easeci.core.workspace.vars.Variable;
import io.easeci.extension.command.VariableType;

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

    static EasefileObjectModel provideEasefileObjectModelWithVars() {
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
                                        new Step(1, "$mockedDirective-{{version}}-beta", "mocked params - stage 3"),
                                        new Step(2, "$git", "pull {{source}} {{branch}} {{strategy}}"),
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

    static EasefileObjectModel provideEasefileObjectModelBrokenBraces() {
        return EasefileObjectModel.builder()
                .stages(List.of(
                        // stage 0
                        Stage.builder()
                                .order(0)
                                .steps(List.of(
                                        new Step(0, "$mockedDirective", "{{variable} params - stage 0"),
                                        new Step(1, "$mockedDirective", "mocked params - stage 0"),
                                        new Step(2, "$mockedDirective", "mocked params - stage 0")
                                        )
                                )
                                .build()
                ))
                .build();
    }

    static EasefileObjectModel provideEasefileObjectModelVariableEasefileAndStage() {
        return EasefileObjectModel.builder()
                .variables(List.of(Variable.of(VariableType.STRING, "branch", "easefile")))
                .stages(List.of(
                        // stage 0
                        Stage.builder()
//                                .variables(List.of(Variable.of(VariableType.STRING, "branch", "stage")))
                                .order(0)
                                .steps(List.of(
                                        new Step(0, "$git", "checkout {{branch}}"),
                                        new Step(1, "$mockedDirective", "mocked params - stage 0"),
                                        new Step(2, "$mockedDirective", "mocked params - stage 0")
                                        )
                                )
                                .build()
                ))
                .build();
    }

    static EasefileObjectModel provideEasefileObjectModelVariableNoVariableDeclaration() {
        return EasefileObjectModel.builder()
                .stages(List.of(
                        // stage 0
                        Stage.builder()
                                .order(0)
                                .steps(List.of(
                                        new Step(0, "$git", "checkout {{branch}}"),
                                        new Step(1, "$mockedDirective", "mocked params - stage 0"),
                                        new Step(2, "$mockedDirective", "mocked params - stage 0")
                                        )
                                )
                                .build()
                ))
                .build();
    }

    static EasefileObjectModel provideEasefileObjectModelVariableEasefileDeclaration() {
        return EasefileObjectModel.builder()
                .variables(List.of(Variable.of(VariableType.STRING, "branch", "develop")))
                .stages(List.of(
                        // stage 0
                        Stage.builder()
                                .order(0)
                                .steps(List.of(
                                        new Step(0, "$git", "checkout {{branch}}"),
                                        new Step(1, "$mockedDirective", "mocked params - stage 0"),
                                        new Step(2, "$mockedDirective", "mocked params - stage 0")
                                        )
                                )
                                .build()
                ))
                .build();
    }

    static EasefileObjectModel provideEasefileObjectModelVariableStageDeclaration() {
        return EasefileObjectModel.builder()
                .variables(List.of(Variable.of(VariableType.STRING, "branch", "stage")))
                .stages(List.of(
                        // stage 0
                        Stage.builder()
                                .order(0)
                                .steps(List.of(
                                        new Step(0, "$git", "checkout {{branch}}"),
                                        new Step(1, "$mockedDirective", "mocked params - stage 0"),
                                        new Step(2, "$mockedDirective", "mocked params - stage 0")
                                        )
                                )
                                .build()
                ))
                .build();
    }
}
