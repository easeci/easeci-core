package io.easeci.core.extension;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

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

    public <T> Optional<T> get(String interfaceName, Class<T> type) {
        T specific = extensionsManager.getPluginContainer().getSpecific(interfaceName, type);
        return Optional.ofNullable(specific);
    }
}
