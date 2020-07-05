package com.meksula;

import io.easeci.extension.ExtensionType;
import io.easeci.extension.Standalone;
import io.easeci.extension.State;
import io.easeci.server.BaseExternalHandlers;
import io.easeci.server.EndpointDeclaration;
import io.easeci.server.EndpointExistsException;
import ratpack.http.HttpMethod;

import static ratpack.http.MediaType.PLAIN_TEXT_UTF8;

public class WelcomePage implements Standalone {
    private final String CONTENT =
            "  ______                   _____ _____                             \n" +
            " |  ____|                 / ____|_   _|                            \n" +
            " | |__   __ _ ___  ___   | |      | |      ___ ___  _ __ ___       \n" +
            " |  __| / _` / __|/ _ \\  | |      | |     / __/ _ \\| '__/ _ \\   \n" +
            " | |___| (_| \\__ \\  __/  | |____ _| |_   | (_| (_) | | |  __/    \n" +
            " |______\\__,_|___/\\___|   \\_____|_____|   \\___\\___/|_|  \\___|\n" +
            " ~ developed by Karol Meksuła 2020                                 \n" +
            "\n";

    @Override
    public void start() {
        try {
            BaseExternalHandlers.register(EndpointDeclaration.builder()
                    .httpMethod(HttpMethod.GET)
                    .endpointUri("")
                    .handler(ctx -> ctx.getResponse().contentType(PLAIN_TEXT_UTF8).send(CONTENT))
                    .build());
        } catch (EndpointExistsException e) {

        }
    }

    @Override
    public void stop() {

    }

    @Override
    public State state() {
        return null;
    }

    @Override
    public String about() {
        return null;
    }

    @Override
    public ExtensionType type() {
        return null;
    }
}
