package io.easeci.api;

import lombok.Data;

@Data
public abstract class Errorable {
    private String errorMessage;

    public static Errorable withError(String errorMessage) {
        Errorable errorable = new Errorable() {};
        errorable.setErrorMessage(errorMessage);
        return errorable;
    }
}
