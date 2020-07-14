package io.easeci.core.extension;

import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.*;
import org.asynchttpclient.netty.request.NettyRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import static io.easeci.core.extension.Utils.pluginFileName;

/* TODO refactor to builder */

@Slf4j
class DefaultPluginDownloader extends PluginDownloader {
    private URL registryUrl;
    private Path targetPath;
    private int timeoutMilliseconds;
    private AsyncHttpClient asyncHttpClient;

    DefaultPluginDownloader(URL registryUrl, Path targetPath, int timeoutMilliseconds, Optional<AsyncHttpClient> optionalClient) {
        super(registryUrl, targetPath, timeoutMilliseconds);
        this.registryUrl = registryUrl;
        this.targetPath = targetPath;
        this.timeoutMilliseconds = timeoutMilliseconds;
        this.asyncHttpClient = optionalClient.orElseGet(this::buildDefaultHttpClient);
    }

    @Override
    Stream<CompletableFuture<Plugin>> download(Set<Plugin> pluginList) {
        return pluginList.stream()
                .map(this::download);
    }

    @Override
    CompletableFuture<Plugin> download(Plugin plugin) {
        try {
            ListenableFuture<File> execute = asyncHttpClient.prepareGet("http://localhost:8080/api/v1/download/" + plugin.getName() + "/" + plugin.getVersion())
                    .execute(new AsyncCompletionHandler<>() {
                        final File futureFile = createEmptyFile(plugin).toFile();
                        final FileOutputStream fileOutputStream = new FileOutputStream(futureFile);

                        @Override
                        public void onRequestSend(NettyRequest request) {
                            log.info("===> Request to registry: {}", "http://localhost:8080/api/v1/download/" + plugin.getName() + "/" + plugin.getVersion());
                        }

                        @Override
                        public State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
                            fileOutputStream.getChannel().write(bodyPart.getBodyByteBuffer());
                            return State.CONTINUE;
                        }

                        @Override
                        public File onCompleted(Response response) throws Exception {
                            log.info("===> {} plugin downloading is complete", plugin.toShortString());
                            return futureFile;
                        }
                    });
            return execute.toCompletableFuture().thenApply(file -> factorize(plugin, file));
        } catch (IOException e) {
            e.printStackTrace();
            return CompletableFuture.supplyAsync(() -> plugin);
        }
    }

    private Path createEmptyFile(Plugin plugin) throws IOException {
        String jarFileName = pluginFileName(plugin.getName(), plugin.getVersion());
        Path fullFilePath = Paths.get(targetPath.toString().concat("/").concat(jarFileName));
        log.info("====> New plugin file created here: {}", fullFilePath);
        return Files.createFile(fullFilePath);
    }

    private AsyncHttpClient buildDefaultHttpClient() {
        DefaultAsyncHttpClientConfig.Builder clientBuilder = Dsl.config().setConnectTimeout(timeoutMilliseconds);
        return Dsl.asyncHttpClient(clientBuilder);
    }

    private Plugin factorize(Plugin plugin, File pluginJarFile) {
//        TODO
        return Plugin.of("", "");
    }
}
