package io.easeci.server;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ratpack.http.HttpMethod;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

/**
 * Final class that has only static content.
 * It was created for enable dynamical adding endpoint feature, by external plugins.
 * @author Karol Meksuła
 * 2020-04-14
 * */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BaseExternalHandlers {
    private static final Set<EndpointDeclaration> endpoints = new HashSet<>();

    /**
     * Register new external endpoint.
     * @param endpointDeclaration is representation of information about endpoint
     * @throws EndpointExistsException when endpoint is just registered and cannot register next same one
     * */
    public static void register(EndpointDeclaration endpointDeclaration) throws EndpointExistsException {
        if (!BaseExternalHandlers.isPathAvailable(endpointDeclaration.getHttpMethod(), endpointDeclaration.getEndpointUri())) {
            throw new EndpointExistsException(endpointDeclaration.getHttpMethod(), endpointDeclaration.getEndpointUri());
        }
        BaseExternalHandlers.endpoints.add(endpointDeclaration);
    }

    /**
     * Fetches ExternalHandlers object that contains all registered endpoints by external plugins
     * */
    public static ExternalHandlers get() {
        return () -> new ArrayList<>(BaseExternalHandlers.endpoints);
    }

    /**
     * List all registered endpoints in string representation
     * */
    public static List<String> listAll() {
        return BaseExternalHandlers.endpoints.stream()
                .map(EndpointDeclaration::getEndpointUri)
                .collect(Collectors.toList());
    }

    /**
     * Clear all endpoints on list.
     * Endpoints just registered will stay without any changes.
     * */
    public static void clear() {
        BaseExternalHandlers.endpoints.clear();
    }

    /**
     * Checks if URI is not registered yet and is available for using in plugin.
     * @return true when path is available
     *         false when path is just taken
     * */
    public static boolean isPathAvailable(HttpMethod httpMethod, String endpointUri) {
        if (isNull(httpMethod) || isNull(endpointUri)) return false;
        return BaseExternalHandlers.endpoints
                .stream()
                .noneMatch(declaration -> declaration.getHttpMethod().equals(httpMethod)
                        && (extractBase(declaration.getEndpointUri()).equals(extractBase(endpointUri))
                        && tokenize(declaration.getEndpointUri()) == tokenize(endpointUri)));
    }

    /**
     * Extract base URI path, cut off parameters etc.
     * @return base URI path without parameters or path variables
     * */
    static String extractBase(String endpointUri) {
        if (isNull(endpointUri)) return "";
        String base = Objects.requireNonNull(endpointUri.split(":")[0]);
        if (base.charAt(base.length() - 1) == '/') {
            return base.substring(0, base.length() - 1);
        }
        return base;
    }

    /**
     * Extract parameters and path variables from URI.
     * @return amount of parameters/path variables in URI
     * */
    static int tokenize(String endpointUri) {
        if (isNull(endpointUri)) return 0;
        List<String> params = Arrays.asList(endpointUri.split(":"));
        params = params.subList(1, params.size());
        return params.size();
    }
}
