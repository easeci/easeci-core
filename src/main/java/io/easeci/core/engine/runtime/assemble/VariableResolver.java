package io.easeci.core.engine.runtime.assemble;

import io.easeci.core.engine.pipeline.EasefileObjectModel;
import io.easeci.core.engine.runtime.VariableResolveException;
import io.easeci.core.workspace.vars.GlobalVariablesFinder;

import static java.util.Objects.isNull;

/**
 * Core abstract, base class for child classes of variable resolving mechanism in EaseCI core.
 * @author Karol Meksuła
 * 2021-04-24
 * */
public abstract class VariableResolver {

    final GlobalVariablesFinder globalVariablesFinder;
    final EasefileObjectModel easefileObjectModel;

    /**
     * Each instance of this class must pass in constructor EasefileObjectModel
     * and auxiliary object to searching for variables in global storage of EaseCI.
     * @param easefileObjectModel is Easefile as a POJO object, there should be no setters, object should be immutable.
     * @param globalVariablesFinder auxiliary object to searching for variables in global storage of EaseCI
     * */
    public VariableResolver(EasefileObjectModel easefileObjectModel, GlobalVariablesFinder globalVariablesFinder) {
        if (isNull(easefileObjectModel) || isNull(globalVariablesFinder)) {
            throw new IllegalStateException("Cannot initialize new VariableResolver with null constructor parameters");
        }
        this.easefileObjectModel = easefileObjectModel;
        this.globalVariablesFinder = globalVariablesFinder;
    }

    /**
     * Only method in this abstract class.
     * Use it to resolve variables for EasefileObjectModel passed in constructor.
     * This method works on state of instance, so it should be immutable.
     * @return EasefileObjectModel with fixed, resolved variables defined in placeholders: {{variable}}
     * @throws VariableResolveException when there are no such variable in Easefile or global storage.
     * */
    public abstract EasefileObjectModel resolve() throws VariableResolveException;
}
