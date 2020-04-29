package io.easeci;

import io.easeci.core.bootstrap.BootstrapperFactory;
import io.easeci.core.extension.ExtensionSystem;
import io.easeci.extension.bootstrap.OnStartup;
import io.easeci.server.ServerBootstrapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EaseciCoreApplication {

    public static void main(String[] args) {
        BootstrapperFactory.factorize().bootstrap(args);

        ExtensionSystem.getInstance().get("io.easeci.extension.bootstrap.OnStartup", OnStartup.class)
                .ifPresentOrElse(OnStartup::action, () -> EaseciCoreApplication.log.error("===> Could not find {} implementation in system", OnStartup.class));

        ExtensionSystem.getInstance().startStandalonePlugins();

        ServerBootstrapper.getInstance().run();
    }
}