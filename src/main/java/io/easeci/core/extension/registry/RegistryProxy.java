package io.easeci.core.extension.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.easeci.commons.YamlUtils;
import io.easeci.core.extension.registry.dto.PluginUpdateCheckResponse;
import lombok.AllArgsConstructor;
import org.asynchttpclient.*;
import ratpack.exec.Promise;

import java.util.Map;

import static io.easeci.core.workspace.LocationUtils.getPluginsYmlLocation;

@AllArgsConstructor
public class RegistryProxy implements PluginUpdate {
    private AsyncHttpClient asyncHttpClient;
    private ObjectMapper objectMapper;
    private String registryUrl;

    public RegistryProxy() {
        this.asyncHttpClient = this.buildDefaultHttpClient();
        this.objectMapper = new ObjectMapper();

        Map<?, ?> yamlValues = YamlUtils.ymlLoad(getPluginsYmlLocation());
        this.registryUrl = (String) YamlUtils.ymlGet(yamlValues, "plugins.registry.url").getValue();
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

    private AsyncHttpClient buildDefaultHttpClient() {
        DefaultAsyncHttpClientConfig.Builder clientBuilder = Dsl.config()
                .setHandshakeTimeout(1000)
                .setConnectTimeout(1000);
        return Dsl.asyncHttpClient(clientBuilder);
    }
}
