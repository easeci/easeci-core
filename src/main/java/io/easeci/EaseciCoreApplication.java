package io.easeci;

import io.easeci.api.client.ClientHandlers;
import io.easeci.api.easefile.EasefileManagementHandlers;
import io.easeci.api.extension.ExtensionHandlers;
import io.easeci.api.log.LogHandler;
import io.easeci.core.bootstrap.BootstrapperFactory;
import io.easeci.core.extension.ExtensionSystem;
import io.easeci.core.extension.PluginSystemCriticalException;
import io.easeci.extension.bootstrap.OnStartup;
import io.easeci.server.BaseExternalHandlers;
import io.easeci.server.ServerBootstrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class EaseciCoreApplication {

    public static void main(String[] args) throws PluginSystemCriticalException {
        BootstrapperFactory.factorize().bootstrap(args);

        ExtensionSystem.getInstance().get("io.easeci.extension.bootstrap.OnStartup", OnStartup.class)
                .ifPresentOrElse(OnStartup::action, () -> EaseciCoreApplication.log.error("===> Could not find {} implementation in system", OnStartup.class));


        ServerBootstrapper.instantiate(List.of(new ExtensionHandlers(), new LogHandler(),
                new ClientHandlers(), new EasefileManagementHandlers()), BaseExternalHandlers.get());
        ServerBootstrapper.getInstance().run();
    }
}