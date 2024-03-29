package io.easeci.server;

import lombok.extern.slf4j.Slf4j;
import ratpack.handling.Chain;
import ratpack.handling.Handler;
import ratpack.http.HttpMethod;
import ratpack.server.RatpackServer;

import java.util.List;

import static java.util.Objects.isNull;
import static ratpack.http.HttpMethod.*;

@Slf4j
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
            RatpackServer.start(server -> server.serverConfig(config -> config.port(9000))
                    .handlers(chain -> {
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
                .forEach(declaration -> {
                    log.info("Registering endpoint, HTTP method: {}, URI: {}", declaration.getHttpMethod(), declaration.getEndpointUri());
                    attach(chain, declaration);
                });
    }

    private void registerExternalEndpoints(Chain chain) {
        if (isNull(this.externalHandlers)) return;
        this.externalHandlers.endpoints().forEach(declaration -> attach(chain, declaration));
    }

    private void attach(Chain chain, EndpointDeclaration declaration) {
        String endpointUri = declaration.getEndpointUri();
        Handler handler = declaration.getHandler();
        HttpMethod method = declaration.getHttpMethod();

        if (declaration.isMultiEndpointDeclaration()) {
            chain.all(declaration.getHandler());
        } else {
            if (GET.equals(method))          chain.get(endpointUri, handler);
            else if (POST.equals(method))    chain.post(endpointUri, handler);
            else if (PATCH.equals(method))   chain.patch(endpointUri, handler);
            else if (PUT.equals(method))     chain.put(endpointUri, handler);
            else if (DELETE.equals(method))  chain.delete(endpointUri, handler);
            else if (OPTIONS.equals(method)) chain.options(endpointUri, handler);
        }
    }
}
