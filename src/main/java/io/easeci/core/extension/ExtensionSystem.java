package io.easeci.core.extension;

import io.easeci.api.extension.ActionRequest;
import io.easeci.api.extension.ActionResponse;
import io.easeci.commons.YamlUtils;
import io.easeci.extension.ExtensionType;
import io.easeci.extension.Standalone;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
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
    @Getter private boolean started = false;
    @Getter private Integer threadPoolMaxSize;
    private ThreadPoolExecutor threadPoolExecutor;

    public static ExtensionSystem getInstance() {
        if (isNull(extensionSystem)) {
            extensionSystem = new ExtensionSystem();
            extensionSystem.extensionsManager = ExtensionsManager.getInstance(getPluginsYmlLocation(), getPluginConfigYmlLocation());
            extensionSystem.threadPoolMaxSize = (Integer) YamlUtils.ymlGet(getPluginsYmlLocation(), "plugins.local.threadpool.max-size").getValue();
            extensionSystem.threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(extensionSystem.threadPoolMaxSize);
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
                .peek(standalone -> {
                    int activeCount = extensionSystem.threadPoolExecutor.getActiveCount();
                    if (activeCount > extensionSystem.threadPoolMaxSize) {
                        log.error("=> Cannot assign new thread for new task. The thread pool is full.");
                        return;
                    }
                    Thread thread = new Thread(standalone::start);
                    thread.setDaemon(true);
//                    Here it will be good to handle Future<?> as a result of submit task TODO
                    extensionSystem.threadPoolExecutor.submit(thread);
//                    Instance instance = (Instance) standalone;
//                    instance.assignThread(thread);
                    /* TODO
                    *    problem jest taki, że mam tutaj tylko goły objekt,
                    *    nie mam informacji o tym w jakim objekcie Instance.class się mój
                    *    goły objekt znajduje. Zadanie polega na skorelowaniu tego.
                    *    Poza tym, oprócz Standalone mam też inne pluginy, więc trzeba będzie
                    *    robić destrukcję tychże objektów, a zatem może będzie trzeba użyć dwóch
                    *    osobnych implementacji takiego destruktora. PluginDestructor.class -- ????
                    * */
                })
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
