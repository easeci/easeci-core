package io.easeci.api.extension;

import io.easeci.core.extension.ExtensionSystem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1")
class ExtensionController {
    private ExtensionSystem extensionSystem;

    ExtensionController() {
        this.extensionSystem = ExtensionSystem.getInstance();
    }

}
