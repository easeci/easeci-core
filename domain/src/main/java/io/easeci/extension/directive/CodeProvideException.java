package io.easeci.extension.directive;

public class CodeProvideException extends Exception {

    private String codeProvideMessage;

    public CodeProvideException(String codeProvideMessage) {
        this.codeProvideMessage = codeProvideMessage;
    }

    @Override
    public String getMessage() {
        return this.codeProvideMessage;
    }
}
