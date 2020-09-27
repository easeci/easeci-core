package io.easeci.core.extension;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.asynchttpclient.*;
import org.asynchttpclient.netty.request.NettyRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelName.PLUGIN_EVENT;
import static io.easeci.core.extension.Utils.completePluginDownloadUrl;
import static io.easeci.core.extension.Utils.pluginFileName;
import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelPrefix.FOUR;
import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelPrefix.THREE;
import static io.easeci.core.log.ApplicationLevelLogFacade.logit;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class DefaultPluginDownloader extends PluginDownloader {
    private URL registryUrl;
    private Path targetPath;
    private int timeoutMilliseconds;
    private AsyncHttpClient asyncHttpClient;
    private AsyncHandler<File> asyncFileHandler;

    @Builder
    DefaultPluginDownloader(URL registryUrl, Path targetPath, int timeoutMilliseconds,
                            AsyncHttpClient asyncHttpClient, AsyncHandler<File> asyncFileHandler) {
        if (isNull(registryUrl)) throw new IllegalArgumentException("'registryUrl' of DefaultPluginDownloader cannot be null");
        if (isNull(targetPath)) throw new IllegalArgumentException("'targetPath' of DefaultPluginDownloader cannot be null!");
        this.registryUrl = registryUrl;
        this.targetPath = targetPath;
        this.timeoutMilliseconds = timeoutMilliseconds;
        this.asyncHttpClient = ofNullable(asyncHttpClient).orElseGet(this::buildDefaultHttpClient);
        this.asyncFileHandler = asyncFileHandler;
    }

    @Override
    Stream<CompletableFuture<Plugin>> download(Set<Plugin> pluginList) {
        return pluginList.stream()
                .map(this::download);
    }

    @Override
    CompletableFuture<Plugin> download(Plugin plugin) {
        return asyncHttpClient.prepareGet(completePluginDownloadUrl(registryUrl, plugin.getName(), plugin.getVersion()))
                .execute(ofNullable(this.asyncFileHandler).orElseGet(() -> this.defaultCompletionHandler(plugin)))
                .toCompletableFuture()
                .thenApply(file -> plugin);
    }

    private AsyncCompletionHandler<File> defaultCompletionHandler(Plugin plugin) {
        try {
            return new AsyncCompletionHandler<>() {
                final File futureFile = createEmptyFile(plugin).toFile();
                final FileOutputStream fileOutputStream = new FileOutputStream(futureFile);

                @Override
                public void onRequestSend(NettyRequest request) {
                    logit(PLUGIN_EVENT, "Request to registry: " + registryUrl.toString(), THREE);
                }

                @Override
                public State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
                    fileOutputStream.getChannel().write(bodyPart.getBodyByteBuffer());
                    return State.CONTINUE;
                }

                @Override
                public File onCompleted(Response response) throws Exception {
                    logit(PLUGIN_EVENT, plugin.toShortString() + " plugin downloading is complete", THREE);
                    return futureFile;
                }

                @Override
                public void onThrowable(Throwable t) {
                    super.onThrowable(t);
                    try {
                        Files.deleteIfExists(futureFile.toPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onTcpConnectFailure(InetSocketAddress remoteAddress, Throwable cause) {
                    logit(PLUGIN_EVENT, "Cannot connect registry! URL: " + remoteAddress.toString(), THREE);
                }

                @Override
                public void onHostnameResolutionFailure(String name, Throwable cause) {
                    logit(PLUGIN_EVENT, "Error occurred while hostname resolving of name: " + name, THREE);
                }
            };
        } catch (IOException e) {
            e.printStackTrace();
            return new AsyncCompletionHandler<>() {
                @Override
                public File onCompleted(Response response) throws Exception {
                    throw new IllegalStateException("Method not implemented!");
                }
            };
        }
    }

    private Path createEmptyFile(Plugin plugin) throws IOException {
        String jarFileName = pluginFileName(plugin.getName(), plugin.getVersion());
        Path fullFilePath = Paths.get(targetPath.toString().concat("/").concat(jarFileName));
        logit(PLUGIN_EVENT, "New plugin file created here: " + fullFilePath, FOUR);
        return Files.createFile(fullFilePath);
    }

    private AsyncHttpClient buildDefaultHttpClient() {
        DefaultAsyncHttpClientConfig.Builder clientBuilder = Dsl.config().setConnectTimeout(timeoutMilliseconds);
        return Dsl.asyncHttpClient(clientBuilder);
    }
}
