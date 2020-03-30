package io.easeci;

import io.easeci.core.bootstrap.BootstrapperFactory;
import io.easeci.core.extension.ExtensionSystem;
import io.easeci.extension.bootstrap.OnStartup;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EaseciCoreApplication {

    public static void main(String[] args) {
        BootstrapperFactory.factorize().bootstrap(args);

        OnStartup onStartup = (OnStartup) ExtensionSystem.getInstance().get("io.easeci.extension.bootstrap.OnStartup");
        onStartup.action();

        SpringApplication.run(EaseciCoreApplication.class, args);
    }
}