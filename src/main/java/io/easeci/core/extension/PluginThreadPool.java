package io.easeci.core.extension;

import io.easeci.commons.YamlUtils;
import io.easeci.extension.Standalone;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static io.easeci.core.workspace.LocationUtils.getPluginsYmlLocation;
import static java.util.Objects.isNull;

@Slf4j
public class PluginThreadPool {
    private static PluginThreadPool instance;
    @Getter private Integer threadPoolMaxSize;
    private ThreadPoolExecutor threadPoolExecutor;
    private PluginContainer pluginContainer;

    public static PluginThreadPool getInstance() throws PluginSystemCriticalException {
        if (isNull(PluginThreadPool.instance)) {
            throw new PluginSystemCriticalException("Cannot getInstance() because it is not created before. " +
                    "First you need to invoke createInstance() method to make application work well");
        }
        return instance;
    }

    public static PluginThreadPool createInstance(PluginContainer pluginContainer) {
        if (isNull(PluginThreadPool.instance)) {
            PluginThreadPool.instance = new PluginThreadPool();
            instance.threadPoolMaxSize = (Integer) YamlUtils.ymlGet(getPluginsYmlLocation(), "plugins.local.threadpool.max-size").getValue();
            instance.threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(instance.threadPoolMaxSize);
            instance.pluginContainer = pluginContainer;
        }
        return instance;
    }

    public List<Standalone> run(List<Standalone> standaloneList) {
        return standaloneList.stream()
                .peek(standalone -> {
                    int activeCount = instance.threadPoolExecutor.getActiveCount();
                    if (activeCount > instance.threadPoolMaxSize) {
                        log.error("=> Cannot assign new thread for new task. The thread pool is full.");
                        return;
                    }
                    Thread thread = new Thread(standalone::start);
                    thread.setDaemon(true);
//                    Here it will be good to handle Future<?> as a result of submit task TODO
                    instance.threadPoolExecutor.submit(thread);
                    int identityHashCode = System.identityHashCode(standalone);
                    pluginContainer.findByIdentityHashCode(identityHashCode)
                            .ifPresentOrElse(instance -> {
                                if (instance.isStandalone()) {
                                    instance.setStarted(true);
                                    instance.assignThread(thread);
                                    log.info("===> [Standalone plugin] Correctly found Instance by hashCode[{}], plugin: {} assigned to running in Thread: {}",
                                            identityHashCode, instance.getPlugin().toShortString(), instance.getThread().toString());
                                } else
                                    log.info("===> [Extension plugin] Correctly found Instance by hashCode[{}], plugin: {}", identityHashCode, instance.getPlugin().toShortString());
                            }, () -> log.error("===> Cannot find Instance by hashCode[{}] of plugin object", identityHashCode));
                }).collect(Collectors.toList());
    }
}
