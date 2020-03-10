package io.easeci;

import io.easeci.core.bootstrap.BootstrapperFactory;
import io.easeci.core.extension.ExtensionsManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EaseciCoreApplication {

    public static void main(String[] args) {
        BootstrapperFactory.factorize().bootstrap(args);

        ExtensionsManager.getInstance().parsePluginFile();

        SpringApplication.run(EaseciCoreApplication.class, args);
    }
}