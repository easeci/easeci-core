package io.easeci;

import io.easeci.core.bootstrap.BootstrapperFactory;
import io.easeci.core.extension.ExtensionSystem;
import io.easeci.extension.bootstrap.OnStartup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class EaseciCoreApplication {

    public static void main(String[] args) {
        BootstrapperFactory.factorize().bootstrap(args);

        ExtensionSystem.getInstance().get("io.easeci.extension.bootstrap.OnStartup", OnStartup.class)
                .ifPresentOrElse(OnStartup::action, () -> log.error("===> Could not find {} implementation in system", OnStartup.class));

        ExtensionSystem.getInstance().startStandalonePlugins();

        SpringApplication.run(EaseciCoreApplication.class, args);
    }
}