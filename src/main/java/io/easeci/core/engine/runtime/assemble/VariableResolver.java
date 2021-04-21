package io.easeci.core.engine.runtime.assemble;

import io.easeci.core.engine.pipeline.EasefileObjectModel;
import io.easeci.core.workspace.vars.GlobalVariablesFinder;

import static java.util.Objects.isNull;

public abstract class VariableResolver {

    final GlobalVariablesFinder globalVariablesFinder;
    final EasefileObjectModel easefileObjectModel;

    public VariableResolver(EasefileObjectModel easefileObjectModel, GlobalVariablesFinder globalVariablesFinder) {
        if (isNull(easefileObjectModel) || isNull(globalVariablesFinder)) {
            throw new IllegalStateException("Cannot initialize new VariableResolver with null constructor parameters");
        }
        this.easefileObjectModel = easefileObjectModel;
        this.globalVariablesFinder = globalVariablesFinder;
    }

    public abstract EasefileObjectModel resolve();
}
