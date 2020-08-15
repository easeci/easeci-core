package io.easeci.api.extension;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.easeci.core.extension.ExtensionControllable;
import io.easeci.core.extension.ExtensionSystem;
import io.easeci.core.extension.PluginSystemCriticalException;
import io.easeci.core.registry.PluginDetails;
import io.easeci.core.registry.PluginUpdate;
import io.easeci.core.registry.RegistryProxy;
import io.easeci.server.EndpointDeclaration;
import io.easeci.server.InternalHandlers;
import ratpack.path.PathTokens;

import java.util.List;

import static ratpack.http.HttpMethod.*;
import static ratpack.http.MediaType.APPLICATION_JSON;

public class ExtensionHandlers implements InternalHandlers {
    private final static String MAPPING = "plugin/";
    private ExtensionControllable controllable;
    private PluginUpdate pluginUpdate;
    private PluginDetails pluginDetails;
    private ObjectMapper objectMapper;

    public ExtensionHandlers() throws PluginSystemCriticalException {
        this.controllable = ExtensionSystem.getInstance();
        final RegistryProxy registryProxy = new RegistryProxy();
        this.pluginUpdate = registryProxy;
        this.pluginDetails = registryProxy;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<EndpointDeclaration> endpoints() {
        return List.of(
                getState(),
                shutdownExtension(),
                enableExtension(),
                restartExtension(),
                checkForUpdate(),
                fetchDetails()
        );
    }

    private EndpointDeclaration getState() {
        return EndpointDeclaration.builder()
                .httpMethod(GET)
                .endpointUri(MAPPING + "state")
                .handler(ctx -> ctx.getRequest().getBody()
                        .map(typedData -> this.controllable.state())
                        .map(pluginContainerState -> objectMapper.writeValueAsBytes(pluginContainerState))
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    private EndpointDeclaration shutdownExtension() {
        return EndpointDeclaration.builder()
                .httpMethod(PATCH)
                .endpointUri(MAPPING + "shutdown")
                .handler(ctx -> ctx.getRequest().getBody()
                        .map(typedData -> objectMapper.readValue(typedData.getBytes(), ActionRequest.class))
                        .map(actionRequest -> this.controllable.shutdownExtension(actionRequest))
                        .map(actionResponse -> objectMapper.writeValueAsBytes(actionResponse))
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    private EndpointDeclaration enableExtension() {
        return EndpointDeclaration.builder()
                .httpMethod(PATCH)
                .endpointUri(MAPPING + "startup")
                .handler(ctx -> ctx.getRequest().getBody()
                        .map(typedData -> objectMapper.readValue(typedData.getBytes(), ActionRequest.class))
                        .map(actionRequest -> this.controllable.startupExtension(actionRequest))
                        .map(actionResponse -> objectMapper.writeValueAsBytes(actionResponse))
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    private EndpointDeclaration restartExtension() {
        return EndpointDeclaration.builder()
                .httpMethod(PATCH)
                .endpointUri(MAPPING + "restart")
                .handler(ctx -> ctx.getRequest().getBody()
                        .map(typedData -> objectMapper.readValue(typedData.getBytes(), ActionRequest.class))
                        .map(actionRequest -> this.controllable.restart(actionRequest))
                        .map(actionResponse -> objectMapper.writeValueAsBytes(actionResponse))
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
                                     .map(pluginUpdateCheckResponse -> this.objectMapper.writeValueAsBytes(pluginUpdateCheckResponse))
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
                            .map(pluginDetailsResponse -> this.objectMapper.writeValueAsBytes(pluginDetailsResponse))
                            .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes));
                }).build();
    }
}
