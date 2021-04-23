package io.easeci.core.engine.runtime;

import java.util.List;

public class VariableResolveException extends Exception {

    private List<VariableResolveException> exceptionList;
    private String message;
    private RuntimeCommunicateType type;

    public VariableResolveException(String message, RuntimeCommunicateType type) {
        this.message = message;
        this.type = type;
    }

    public VariableResolveException(List<VariableResolveException> exceptionList, RuntimeCommunicateType type) {
        this.exceptionList = exceptionList;
        this.message = "Many VariableResolveException was detected";
        this.type = type;
    }

    @Override
    public String getMessage() {
        return "VariableResolveException with type: " + type + ", " + message;
    }

    public List<VariableResolveException> getExceptionList() {
        return exceptionList;
    }
}
