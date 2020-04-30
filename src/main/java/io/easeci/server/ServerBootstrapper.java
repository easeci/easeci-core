package io.easeci.server;

import ratpack.handling.Chain;
import ratpack.handling.Handler;
import ratpack.server.RatpackServer;

import java.util.List;

import static io.vavr.API.*;
import static java.util.Objects.isNull;
import static ratpack.http.HttpMethod.*;

public class ServerBootstrapper {
    private static ServerBootstrapper bootstrapper;
    private ExternalHandlers externalHandlers;
    private List<InternalHandlers> internalHandlers;

    public static ServerBootstrapper getInstance() {
        if (isNull(ServerBootstrapper.bootstrapper)) {
            throw new IllegalStateException("ServerBootstrapper is not initialized correctly. Use instantiate(..) method and next you can retrieve this one");
        }
        return bootstrapper;
    }

    /**
     * Server will initialize endpoints based on two parameters
     * @param internalHandlers that just defines endpoints embedded in base form of EaseCI Core
     * @param externalHandlers that defines endpoints that comes from external source like yaml/json config file etc.
     * */
    public static ServerBootstrapper instantiate(List<InternalHandlers> internalHandlers, ExternalHandlers externalHandlers) {
        if (isNull(ServerBootstrapper.bootstrapper)) {
            ServerBootstrapper.bootstrapper = new ServerBootstrapper(internalHandlers, externalHandlers);
        }
        return bootstrapper;
    }

    private ServerBootstrapper(List<InternalHandlers> internalHandlers, ExternalHandlers externalHandlers) {
        this.internalHandlers = internalHandlers;
        this.externalHandlers = externalHandlers;
    }

    public void run() {
        try {
            RatpackServer.start(server -> server.handlers(chain -> {
                registerInternalEndpoints(chain);
                registerExternalEndpoints(chain);
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerInternalEndpoints(Chain chain) {
        if (isNull(this.internalHandlers)) return;
        this.internalHandlers.stream()
                .flatMap(internal -> internal.endpoints().stream())
                .forEach(declaration -> attach(chain, declaration));
    }

    private void registerExternalEndpoints(Chain chain) {
        if (isNull(this.externalHandlers)) return;
        this.externalHandlers.endpoints().forEach(declaration -> attach(chain, declaration));
    }

    private void attach(Chain chain, EndpointDeclaration declaration) {
        String endpointUri = declaration.getEndpointUri();
        Handler handler = declaration.getHandler();
        Match(declaration.getHttpMethod())
                .of(
                        Case($(GET), chain.get(endpointUri, handler)),
                        Case($(POST), chain.post(endpointUri, handler)),
                        Case($(PATCH), chain.patch(endpointUri, handler)),
                        Case($(PUT), chain.put(endpointUri, handler)),
                        Case($(DELETE), chain.delete(endpointUri, handler)),
                        Case($(OPTIONS), chain.options(endpointUri, handler))
                );
    }
}
