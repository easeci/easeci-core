package io.easeci.api.extension;

import io.easeci.api.validation.ApiRequestValidator;
import io.easeci.commons.SerializeUtils;
import io.easeci.core.extension.DirectivesCollector;
import io.easeci.core.extension.ExtensionControllable;
import io.easeci.core.extension.ExtensionSystem;
import io.easeci.core.extension.PluginSystemCriticalException;
import io.easeci.core.registry.PluginDetails;
import io.easeci.core.registry.PluginUpdate;
import io.easeci.core.registry.RegistryProxy;
import io.easeci.server.EndpointDeclaration;
import io.easeci.server.InternalHandlers;
import ratpack.exec.Promise;
import ratpack.path.PathTokens;

import java.util.List;

import static io.easeci.api.validation.ApiRequestValidator.extractBody;
import static ratpack.http.HttpMethod.*;
import static ratpack.http.MediaType.APPLICATION_JSON;

public class ExtensionHandlers implements InternalHandlers {
    private final static String MAPPING = "plugin/";
    private ExtensionControllable controllable;
    private DirectivesCollector directivesCollector;
    private PluginUpdate pluginUpdate;
    private PluginDetails pluginDetails;

    public ExtensionHandlers() throws PluginSystemCriticalException {
        ExtensionSystem extensionSystem = ExtensionSystem.getInstance();
        this.controllable = extensionSystem;
        this.directivesCollector = extensionSystem;
        final RegistryProxy registryProxy = new RegistryProxy();
        this.pluginUpdate = registryProxy;
        this.pluginDetails = registryProxy;
    }

    @Override
    public List<EndpointDeclaration> endpoints() {
        return List.of(
                getState(),
                shutdownExtension(),
                enableExtension(),
                restartExtension(),
                checkForUpdate(),
                fetchDetails(),
                fetchAllAvailableDirectives()
        );
    }

    private EndpointDeclaration getState() {
        return EndpointDeclaration.builder()
                .httpMethod(GET)
                .endpointUri(MAPPING + "state")
                .handler(ctx -> ctx.getRequest().getBody()
                        .map(typedData -> this.controllable.state())
                        .map(SerializeUtils::write)
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    private EndpointDeclaration shutdownExtension() {
        return EndpointDeclaration.builder()
                .httpMethod(PATCH)
                .endpointUri(MAPPING + "shutdown")
                .handler(ctx -> extractBody(ctx.getRequest(), ActionRequest.class)
                        .map(actionRequest -> this.controllable.shutdownExtension(actionRequest))
                        .mapError(throwable -> ActionResponse.of(false, List.of(throwable.getMessage())))
                        .map(SerializeUtils::write)
                        .mapError(ApiRequestValidator::handleException)
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    private EndpointDeclaration enableExtension() {
        return EndpointDeclaration.builder()
                .httpMethod(PATCH)
                .endpointUri(MAPPING + "startup")
                .handler(ctx -> extractBody(ctx.getRequest(), ActionRequest.class)
                        .map(actionRequest -> this.controllable.startupExtension(actionRequest))
                        .map(SerializeUtils::write)
                        .mapError(ApiRequestValidator::handleException)
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    private EndpointDeclaration restartExtension() {
        return EndpointDeclaration.builder()
                .httpMethod(PATCH)
                .endpointUri(MAPPING + "restart")
                .handler(ctx -> extractBody(ctx.getRequest(), ActionRequest.class)
                        .map(actionRequest -> this.controllable.restart(actionRequest))
                        .map(SerializeUtils::write)
                        .mapError(ApiRequestValidator::handleException)
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    private EndpointDeclaration checkForUpdate() {
        final String PLUGIN_NAME = "pluginName",
                  PLUGIN_VERSION = "pluginVersion";
        return EndpointDeclaration.builder()
                .httpMethod(GET)
                .endpointUri(MAPPING + "update/check/:" + PLUGIN_NAME + "/:" + PLUGIN_VERSION)
                .handler(ctx -> {
                    PathTokens pathTokens = ctx.getPathTokens();
                    String requestedPluginName = pathTokens.get(PLUGIN_NAME);
                    String requestedPluginVersion = pathTokens.get(PLUGIN_VERSION);
                    this.pluginUpdate.checkForUpdate(requestedPluginName, requestedPluginVersion)
                                     .map(SerializeUtils::write)
                                     .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes));
                }).build();
    }

    private EndpointDeclaration fetchDetails() {
        final String PLUGIN_NAME = "pluginName",
                  PLUGIN_VERSION = "pluginVersion";
        return EndpointDeclaration.builder()
                .httpMethod(GET)
                .endpointUri(MAPPING + "details/:" + PLUGIN_NAME + "/:" + PLUGIN_VERSION)
                .handler(ctx -> {
                    PathTokens pathTokens = ctx.getPathTokens();
                    String requestedPluginName = pathTokens.get(PLUGIN_NAME);
                    String requestedPluginVersion = pathTokens.get(PLUGIN_VERSION);
                    this.pluginDetails.fetchDetails(requestedPluginName, requestedPluginVersion)
                            .map(SerializeUtils::write)
                            .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes));
                }).build();
    }

    // find all directives from all installed plugins in EaseCI system
    private EndpointDeclaration fetchAllAvailableDirectives() {
        return EndpointDeclaration.builder()
                .httpMethod(GET)
                .endpointUri(MAPPING + "directives")
                .handler(ctx -> Promise.value(directivesCollector.collectAll())
                        .map(SerializeUtils::write)
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }
}
