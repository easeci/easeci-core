package io.easeci.core.extension;

import io.easeci.api.extension.ActionRequest;
import io.easeci.api.extension.ActionResponse;
import io.easeci.extension.ExtensionType;
import io.easeci.extension.Standalone;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.easeci.core.workspace.LocationUtils.getPluginConfigYmlLocation;
import static io.easeci.core.workspace.LocationUtils.getPluginsYmlLocation;
import static java.util.Objects.isNull;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExtensionSystem implements ExtensionControllable {
    private final static String STANDALONE_INTERFACE = "io.easeci.extension.Standalone";
    private final static Class<Standalone> STANDALONE_CLASS = Standalone.class;
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

    public synchronized <T> List<T> getAll(String interfaceName, Class<T> type) {
        if (!started) {
            throw new RuntimeException("==> Cannot get some reference from container because ExtensionSystem is not started yet");
        }
        return extensionsManager.getPluginContainer().getGathered(interfaceName, type);
    }

    /**
     * Start all standalone plugins declared in plugins.yml file
     * @return list of Standalone instances just ran
     * */
    public List<Standalone> startStandalonePlugins() {
        return this.getAll(STANDALONE_INTERFACE, STANDALONE_CLASS)
                .stream()
                .distinct()
                .peek(Standalone::start)
                .collect(Collectors.toList());
    }

    @Override
    public PluginContainerState state(ExtensionType extensionType) {
        return ((ExtensionControllable) this.extensionsManager).state(extensionType);
    }

    @Override
    public ActionResponse shutdownExtension(ActionRequest actionRequest) {
        return ((ExtensionControllable) this.extensionsManager).shutdownExtension(actionRequest);
    }

    @Override
    public ActionResponse startupExtension(ActionRequest actionRequest) {
        return ((ExtensionControllable) this.extensionsManager).startupExtension(actionRequest);
    }

    @Override
    public ActionResponse restart(ActionRequest actionRequest) {
        return ((ExtensionControllable) this.extensionsManager).restart(actionRequest);
    }
}
