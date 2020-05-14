package io.easeci.server;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BaseExternalHandlers {
    private static final Set<EndpointDeclaration> endpoints = new HashSet<>();

    public static void register(EndpointDeclaration endpointDeclaration) {
        BaseExternalHandlers.endpoints.add(endpointDeclaration);
    }

    public static ExternalHandlers get() {
        return () -> new ArrayList<>(BaseExternalHandlers.endpoints);
    }
}
