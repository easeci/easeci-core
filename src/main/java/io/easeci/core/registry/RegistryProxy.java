package io.easeci.core.registry;

import io.easeci.commons.SerializeUtils;
import io.easeci.commons.YamlUtils;
import io.easeci.core.extension.ExtensionSystem;
import io.easeci.core.extension.PluginSystemCriticalException;
import io.easeci.core.registry.dto.PluginDetailsResponse;
import io.easeci.core.registry.dto.PluginUpdateCheckResponse;
import io.easeci.core.registry.dto.ZippedPluginDetailsResponse;
import lombok.AllArgsConstructor;
import org.asynchttpclient.*;
import ratpack.exec.Promise;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static io.easeci.core.workspace.LocationUtils.getPluginsYmlLocation;
import static java.util.Objects.isNull;

@AllArgsConstructor
public class RegistryProxy implements PluginUpdate, PluginDetails {
    private ExtensionSystem extensionSystem;
    private AsyncHttpClient asyncHttpClient;
    private String registryUrl;
    private Boolean fetchDocumentation;

    public RegistryProxy() {
        this.asyncHttpClient = this.buildDefaultHttpClient();

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
                .map(bytes -> SerializeUtils.read(bytes, PluginUpdateCheckResponse.class).orElseThrow());
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
        return SerializeUtils.read(bytes, PluginDetailsResponse.class).orElseThrow();
    }

    private AsyncHttpClient buildDefaultHttpClient() {
        DefaultAsyncHttpClientConfig.Builder clientBuilder = Dsl.config()
                .setHandshakeTimeout(1000)
                .setConnectTimeout(1000);
        return Dsl.asyncHttpClient(clientBuilder);
    }

}
