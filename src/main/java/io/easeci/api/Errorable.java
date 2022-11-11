package io.easeci.api;

import lombok.Data;

/**
 * All child instances should extend this abstract class
 * when we expect an error on API response.
 * */
@Data
public abstract class Errorable {
    private String errorMessage;

    public static Errorable withError(String errorMessage) {
        Errorable errorable = new Errorable() {};
        errorable.setErrorMessage(errorMessage);
        return errorable;
    }

    public static Errorable withError(Throwable throwable) {
        Errorable errorable = new Errorable() {};
        errorable.setErrorMessage(throwable.getMessage());
        return errorable;
    }

}
