package io.easeci.api.extension;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.easeci.core.extension.ExtensionControllable;
import io.easeci.core.extension.ExtensionSystem;
import io.easeci.core.extension.PluginContainerState;
import io.easeci.extension.ExtensionType;
import io.easeci.server.EndpointDeclaration;
import io.easeci.server.InternalHandlers;

import java.util.List;

import static ratpack.http.HttpMethod.*;
import static ratpack.http.MediaType.APPLICATION_JSON;

public class ExtensionHandlers implements InternalHandlers {
    private ExtensionControllable controllable;
    private ObjectMapper objectMapper;

    public ExtensionHandlers() {
        this.controllable = ExtensionSystem.getInstance();
        this.objectMapper = new ObjectMapper();
    }

    private EndpointDeclaration getState() {
        return EndpointDeclaration.builder()
                .httpMethod(GET)
                .endpointUri("state/:extensionType")
                .handler(ctx -> {
                    String extensionType = ctx.getPathTokens().get("extensionType").toUpperCase();
                    PluginContainerState containerState = this.controllable.state(ExtensionType.valueOf(extensionType));
                    ctx.getResponse().contentType(APPLICATION_JSON);
                    byte[] responseAsBytes = objectMapper.writeValueAsBytes(containerState);
                    ctx.getResponse().send(responseAsBytes);
                })
                .build();
    }

    @Override
    public List<EndpointDeclaration> endpoints() {
        return List.of(
                getState()
        );
    }

//
//    @PatchMapping("/shutdown")
//    @ResponseStatus(HttpStatus.OK)
//    Mono<ActionResponse> shutdownExtension(@RequestBody ActionRequest actionRequest) {
//        return Mono.just(this.controllable.shutdownExtension(actionRequest));
//    }
//
//    @PatchMapping("/startup")
//    @ResponseStatus(HttpStatus.OK)
//    Mono<ActionResponse> enableExtension(@RequestBody ActionRequest actionRequest) {
//        return Mono.just(this.controllable.startupExtension(actionRequest));
//    }
//
//    @PatchMapping("/restart")
//    @ResponseStatus(HttpStatus.OK)
//    Mono<ActionResponse> restartExtension(@RequestBody ActionRequest actionRequest) {
//        return Mono.just(this.controllable.restart(actionRequest));
//    }
}
