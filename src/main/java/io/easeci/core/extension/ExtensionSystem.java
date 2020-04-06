package io.easeci.core.extension;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import static io.easeci.core.workspace.LocationUtils.getPluginConfigYmlLocation;
import static io.easeci.core.workspace.LocationUtils.getPluginsYmlLocation;
import static java.util.Objects.isNull;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExtensionSystem {
    private static ExtensionSystem extensionSystem;
    private ExtensionsManager extensionsManager;
    private boolean started = false;

    public static ExtensionSystem getInstance() {
        if (isNull(extensionSystem)) {
            extensionSystem = new ExtensionSystem();
            extensionSystem.extensionsManager = ExtensionsManager.getInstance(getPluginsYmlLocation(), getPluginConfigYmlLocation());
        }
        return extensionSystem;
    }

    public synchronized void start() {
        if (started) {
            log.error("===> Extension system is just started correctly");
            return;
        }
        this.started = true;
        this.extensionsManager.enableExtensions();
    }

    public synchronized <T> Optional<T> get(String interfaceName, Class<T> type) {
        if (!started) {
            throw new RuntimeException("==> Cannot get some reference from container because ExtensionSystem is not started yet");
        }
        T specific = extensionsManager.getPluginContainer().getSpecific(interfaceName, type);
        return Optional.ofNullable(specific);
    }
}
