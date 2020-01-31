package io.easeci.core.bootstrap;

import lombok.extern.slf4j.Slf4j;

/**
 * Simple factory that creates Bootstrapper instance depends on
 * Operating System name.
 * @author Karol Meksuła
 * 2020-01-31
 * */

@Slf4j
public class BootstrapperFactory {

    public static Bootstrapper factorize() {
        if (checkOS().equals(Platform.LINUX)) {
            return LinuxBootstrapper.getInstance();
        }
        throw new RuntimeException("Any OS platform matching! This application cannot support your OS");
    }

    private static Platform checkOS() {
        Platform platform = Platform.valueOf(System.getProperty("os.name").toUpperCase());
        log.info("==> OS system detected: " + platform.name());
        return platform;
    }
}
