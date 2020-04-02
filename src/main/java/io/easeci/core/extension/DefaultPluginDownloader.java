package io.easeci.core.extension;

import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

@Slf4j
class DefaultPluginDownloader extends PluginDownloader {
    private URL registryUrl;
    private Path targetPath;
    private long timeoutMilliseconds;

    DefaultPluginDownloader(URL registryUrl, Path targetPath, long timeoutMilliseconds) {
        super(registryUrl, targetPath, timeoutMilliseconds);
        this.registryUrl = registryUrl;
        this.targetPath = targetPath;
        this.timeoutMilliseconds = timeoutMilliseconds;
    }

    @Override
    CompletableFuture<Plugin> download(Plugin plugin) {
//        TODO implement!
        log.info("Method is not implemented yet");
        return CompletableFuture.supplyAsync(() -> Plugin.of("empty", "1.0"));
    }
}
