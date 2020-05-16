package io.easeci.server;

import ratpack.http.HttpMethod;

public class EndpointExistsException extends Exception {

    public EndpointExistsException(HttpMethod httpMethod, String endpointPath) {
        super("Cannot create endpoint [method: " + httpMethod.toString() + "] with path: " + endpointPath + ", because this one just exists!");
    }
}
