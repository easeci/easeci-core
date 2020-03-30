package io.easeci.core.extension;

import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

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
        throw new IllegalStateException("Method is not implemented yet");
    }
}
