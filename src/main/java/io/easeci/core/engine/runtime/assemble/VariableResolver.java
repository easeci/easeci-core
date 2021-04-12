package io.easeci.core.engine.runtime.assemble;

import io.easeci.core.engine.pipeline.EasefileObjectModel;

public interface VariableResolver {

    EasefileObjectModel resolve(EasefileObjectModel eom);
}
