package io.easeci.core.extension;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.isNull;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExtensionSystem {
    private static ExtensionSystem extensionSystem;
    private ExtensionsManager extensionsManager = ExtensionsManager.getInstance();

    public static ExtensionSystem getInstance() {
        if (isNull(extensionSystem)) {
            extensionSystem = new ExtensionSystem();
        }
        return extensionSystem;
    }

    public void start() {
        this.extensionsManager.enableExtensions();
    }

    public Object get(String interfaceName) {
        return extensionsManager.getPluginContainer().get(interfaceName);
    }
}
