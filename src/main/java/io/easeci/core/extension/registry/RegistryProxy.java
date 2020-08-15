package io.easeci.core.extension.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.easeci.commons.YamlUtils;
import io.easeci.core.extension.ExtensionSystem;
import io.easeci.core.extension.PluginSystemCriticalException;
import io.easeci.core.extension.registry.dto.PluginDetailsResponse;
import io.easeci.core.extension.registry.dto.PluginUpdateCheckResponse;
import io.easeci.core.extension.registry.dto.ZippedPluginDetailsResponse;
import lombok.AllArgsConstructor;
import org.asynchttpclient.*;
import ratpack.exec.Promise;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static io.easeci.core.workspace.LocationUtils.getPluginsYmlLocation;
import static java.util.Objects.isNull;

@AllArgsConstructor
public class RegistryProxy implements PluginUpdate, PluginDetails {
    private ExtensionSystem extensionSystem;
    private AsyncHttpClient asyncHttpClient;
    private ObjectMapper objectMapper;
    private String registryUrl;
    private Boolean fetchDocumentation;

    public RegistryProxy() {
        this.asyncHttpClient = this.buildDefaultHttpClient();
        this.objectMapper = new ObjectMapper();

        Map<?, ?> yamlValues = YamlUtils.ymlLoad(getPluginsYmlLocation());
        this.registryUrl = (String) YamlUtils.ymlGet(yamlValues, "plugins.registry.url").getValue();
        this.fetchDocumentation = (Boolean) YamlUtils.ymlGet(yamlValues, "plugins.details.fetch-documentation").getValue();
        if (isNull(fetchDocumentation))
            this.fetchDocumentation = false;

        this.extensionSystem = getExtensionSystem();
    }

    private ExtensionSystem getExtensionSystem() {
        try {
            return ExtensionSystem.getInstance();
        } catch (PluginSystemCriticalException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Promise<PluginUpdateCheckResponse> checkForUpdate(String pluginName, String pluginVersion) {
        return Promise.toPromise(asyncHttpClient.executeRequest(
                new RequestBuilder()
                        .setMethod("GET")
                        .setUrl(this.registryUrl + "/api/v1/update/" + pluginName + "/" + pluginVersion)
                        .build())
                .toCompletableFuture())
                .map(Response::getResponseBodyAsBytes)
                .map(bytes -> objectMapper.readValue(bytes, PluginUpdateCheckResponse.class));
    }

    @Override
    public Promise<ZippedPluginDetailsResponse> fetchDetails(String pluginName, String pluginVersion) {
        return Promise.toPromise(asyncHttpClient.executeRequest(
                new RequestBuilder()
                        .setMethod("GET")
                        .setUrl(this.registryUrl + "/api/v1/details/" + pluginName + "/" + pluginVersion + "?documentation=" + fetchDocumentation)
                        .build())
                .toCompletableFuture()
                .thenApply(Response::getResponseBodyAsBytes)
                .thenApply(this::read)
                .thenCombine(CompletableFuture.supplyAsync(() -> extensionSystem)
                        .thenApply(es -> es.state().getPluginStates()
                                .stream()
                                .filter(pluginState -> pluginState.getPluginName().equals(pluginName)
                                        && pluginState.getPluginVersion().equals(pluginVersion))
                                .findAny()
                                .orElse(null)), ZippedPluginDetailsResponse::of));
    }

    private PluginDetailsResponse read(byte[] bytes) {
        try {
            return objectMapper.readValue(bytes, PluginDetailsResponse.class);
        } catch (IOException e) {
            throw new RuntimeException("Some error occurred while receiving bytes from registry's response body");
        }
    }

    private AsyncHttpClient buildDefaultHttpClient() {
        DefaultAsyncHttpClientConfig.Builder clientBuilder = Dsl.config()
                .setHandshakeTimeout(1000)
                .setConnectTimeout(1000);
        return Dsl.asyncHttpClient(clientBuilder);
    }

}
