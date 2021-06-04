package io.easeci.server;

import io.easeci.core.engine.runtime.PipelineContextNotExists;
import io.easeci.core.engine.runtime.PipelineContextSystem;
import io.easeci.core.engine.runtime.logs.LogRail;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import ratpack.handling.Chain;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.http.HttpMethod;
import ratpack.server.RatpackServer;
import ratpack.websocket.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

import static java.util.Objects.isNull;
import static ratpack.http.HttpMethod.*;
import static ratpack.stream.Streams.periodically;
import static ratpack.stream.Streams.publish;

@Slf4j
public class ServerBootstrapper {
    private static ServerBootstrapper bootstrapper;
    private ExternalHandlers externalHandlers;
    private List<InternalHandlers> internalHandlers;

    public static ServerBootstrapper getInstance() {
        if (isNull(ServerBootstrapper.bootstrapper)) {
            throw new IllegalStateException("ServerBootstrapper is not initialized correctly. Use instantiate(..) method and next you can retrieve this one");
        }
        return bootstrapper;
    }

    /**
     * Server will initialize endpoints based on two parameters
     * @param internalHandlers that just defines endpoints embedded in base form of EaseCI Core
     * @param externalHandlers that defines endpoints that comes from external source like yaml/json config file etc.
     * */
    public static ServerBootstrapper instantiate(List<InternalHandlers> internalHandlers, ExternalHandlers externalHandlers) {
        if (isNull(ServerBootstrapper.bootstrapper)) {
            ServerBootstrapper.bootstrapper = new ServerBootstrapper(internalHandlers, externalHandlers);
        }
        return bootstrapper;
    }

    private ServerBootstrapper(List<InternalHandlers> internalHandlers, ExternalHandlers externalHandlers) {
        this.internalHandlers = internalHandlers;
        this.externalHandlers = externalHandlers;
    }

    public void run() {
        try {
            RatpackServer.start(server -> server.handlers(chain -> {
                registerInternalEndpoints(chain);
                registerExternalEndpoints(chain);
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerInternalEndpoints(Chain chain) {
        if (isNull(this.internalHandlers)) return;
        this.internalHandlers.stream()
                .flatMap(internal -> internal.endpoints().stream())
                .forEach(declaration -> attach(chain, declaration));
    }

    private void registerExternalEndpoints(Chain chain) {
        if (isNull(this.externalHandlers)) return;
        this.externalHandlers.endpoints().forEach(declaration -> attach(chain, declaration));
    }

    private void attach(Chain chain, EndpointDeclaration declaration) {
        String endpointUri = declaration.getEndpointUri();
        Handler handler = declaration.getHandler();
        HttpMethod method = declaration.getHttpMethod();

        final String PIPELINE_CONTEXT_ID = "pipelineContextId";

        chain.get("ws/log/:pipelineContextId", new Handler() {
            @Override
            public void handle(Context ctx) throws Exception {
                WebSockets.websocket(ctx, new WebSocketHandler<String>() {
                    @Override
                    public String onOpen(WebSocket webSocket) throws Exception {
//                        webSocket.send(finalOpenResponseCommunicate);
                        final String pipelineContextIdString = ctx.getAllPathTokens().get(PIPELINE_CONTEXT_ID);
                        String openResponseCommunicate = "connected";
                        LogRail logRail = null;
                        try {
                            UUID pipelineContextId = UUID.fromString(pipelineContextIdString);
                            logRail = PipelineContextSystem.getInstance().getLogRail(pipelineContextId);
                            logRail.onPublish(text -> {
                                log.info("Sent: {}", text);
                                webSocket.send(text);
                            });
                            logRail.initPublishing();
                        }
                        catch (PipelineContextNotExists e) {
                            openResponseCommunicate = e.getMessage();
                        } catch (Throwable t) {
                            String msg = "Bad input. " + pipelineContextIdString + " is not correct UUID";
                            log.error(msg);
                            openResponseCommunicate = msg;
                        }

                        webSocket.send(openResponseCommunicate);
                        return "ok";
                    }

                    @Override
                    public void onClose(WebSocketClose<String> close) throws Exception {
                    }

                    @Override
                    public void onMessage(WebSocketMessage<String> frame) throws Exception {
                        System.out.println(frame.getText());
                    }
                });
////
//
//                WebSockets.websocketBroadcast(ctx, publish(List.of("Działa czy nie...")));
////
//                String openResponseCommunicate = "Connected";
//                LogRail logRail = null;
//                try {
//                    UUID pipelineContextId = UUID.fromString(pipelineContextIdString);
//                    logRail = PipelineContextSystem.getInstance().getLogRail(pipelineContextId);
//                    logRail.onPublish(strings -> WebSockets.websocketBroadcast(ctx, publish(strings)));
//                }
//                catch (PipelineContextNotExists e) {
//                    openResponseCommunicate = e.getMessage();
//                } catch (Throwable t) {
//                    String msg = "Bad input. " + pipelineContextIdString + " is not correct UUID";
//                    log.error(msg);
//                    openResponseCommunicate = msg;
//                }
//                Publisher<String> stream = periodically(ctx, Duration.ofSeconds(1),
//                        new Function<Integer, String>() {
//                            @Override
//                            public String apply(Integer i) throws Exception {
//                                log.info("New message on ws channel: " + i);
//                                return i < 5 ? i.toString() : null;
//                            }
//                        }
//                );
//                periodically(ctx, Duration.ofMillis(100),
//                        new Function<Integer, String>() {
//                            @Override
//                            public String apply(Integer i) throws Exception {
//                                stringList.add(LocalDateTime.now().toString());
//                                return i < 50 ? i.toString() : null;
//                            }
//                        }
//                );
//
////
////
//                final String finalOpenResponseCommunicate = openResponseCommunicate;
////
////
//                assert logRail != null;
//                logRail.initPublishing();
            }
        }
        );

        if (GET.equals(method))          chain.get(endpointUri, handler);
        else if (POST.equals(method))    chain.post(endpointUri, handler);
        else if (PATCH.equals(method))   chain.patch(endpointUri, handler);
        else if (PUT.equals(method))     chain.put(endpointUri, handler);
        else if (DELETE.equals(method))  chain.delete(endpointUri, handler);
        else if (OPTIONS.equals(method)) chain.options(endpointUri, handler);
    }
}
