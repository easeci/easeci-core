package io.easeci.server;

import java.util.List;

interface Handlers {
    /**
     * @return list of EndpointDeclaration objects that should be added
     *         to application runtime
     * */
    List<EndpointDeclaration> endpoints();
}
