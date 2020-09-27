package io.easeci.core.extension;

import io.easeci.commons.YamlUtils;
import io.easeci.extension.Standalone;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelName.PLUGIN_EVENT;
import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelPrefix.ONE;
import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelPrefix.THREE;
import static io.easeci.core.log.ApplicationLevelLogFacade.logit;
import static io.easeci.core.workspace.LocationUtils.getPluginsYmlLocation;
import static java.util.Objects.isNull;

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
                        logit(PLUGIN_EVENT, "Cannot assign new thread for new task. The thread pool is full.", ONE);
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
                                    logit(PLUGIN_EVENT, "[Standalone plugin] Correctly found Instance by hashCode["
                                            + identityHashCode + "], plugin: " + instance.getPlugin().toShortString()
                                            + " assigned to running in Thread: " + instance.getThread().toString(), THREE);
                                } else
                                    logit(PLUGIN_EVENT, "[Extension plugin] Correctly found Instance by hashCode[" + identityHashCode + "], plugin: " + instance.getPlugin().toShortString(), THREE);
                            }, () -> logit(PLUGIN_EVENT,"Cannot find Instance by hashCode[" + identityHashCode + "] of plugin object", THREE));
                }).collect(Collectors.toList());
    }
}
