package io.easeci.core.engine.pipeline;

import io.easeci.extension.command.Command;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class Stage {
    private String name;
    private int order;
    private List<Command> commands;
}
