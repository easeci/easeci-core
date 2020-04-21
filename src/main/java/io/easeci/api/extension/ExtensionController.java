package io.easeci.api.extension;

import io.easeci.core.extension.ExtensionControllable;
import io.easeci.core.extension.ExtensionSystem;
import io.easeci.core.extension.PluginContainerState;
import io.easeci.extension.ExtensionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/extension")
class ExtensionController {
    private ExtensionControllable controllable;

    ExtensionController() {
        this.controllable = ExtensionSystem.getInstance();
    }

    @GetMapping("/state/{extensionType}")
    @ResponseStatus(HttpStatus.OK)
    Mono<PluginContainerState> getState(@PathVariable ExtensionType extensionType) {
        return Mono.just(this.controllable.state(extensionType));
    }

    @PatchMapping("/shutdown")
    @ResponseStatus(HttpStatus.OK)
    Mono<ActionResponse> shutdownExtension(@RequestBody ActionRequest actionRequest) {
        return Mono.just(this.controllable.shutdownExtension(actionRequest));
    }

    @PatchMapping("/startup")
    @ResponseStatus(HttpStatus.OK)
    Mono<ActionResponse> enableExtension(@RequestBody ActionRequest actionRequest) {
        return Mono.just(this.controllable.startupExtension(actionRequest));
    }

    @PatchMapping("/restart")
    @ResponseStatus(HttpStatus.OK)
    Mono<ActionResponse> restartExtension(@RequestBody ActionRequest actionRequest) {
        return Mono.just(this.controllable.restart(actionRequest));
    }
}
