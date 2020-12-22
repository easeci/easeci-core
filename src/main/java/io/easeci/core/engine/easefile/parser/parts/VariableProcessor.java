package io.easeci.core.engine.easefile.parser.parts;

import io.easeci.core.engine.easefile.parser.analyse.SyntaxError;
import io.easeci.core.workspace.vars.Variable;
import io.vavr.Tuple2;

import java.util.List;
import java.util.Optional;

public class VariableProcessor implements PipelinePartProcessor<List<Variable>> {

    @Override
    public Tuple2<Optional<List<Variable>>, List<SyntaxError>> process() {
        return null;
    }
}
