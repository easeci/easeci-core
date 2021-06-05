package io.easeci.api.socket;

import io.easeci.server.EndpointDeclaration;
import io.easeci.server.InternalHandlers;
import lombok.extern.slf4j.Slf4j;
import ratpack.http.HttpMethod;
import ratpack.websocket.*;

import java.util.List;

@Slf4j
public class WebSocketHandlers implements InternalHandlers {

    private Commands commands;

    public WebSocketHandlers() {
        this.commands = new Commands();
    }

    @Override
    public List<EndpointDeclaration> endpoints() {
        return List.of(logStreamSocket());
    }

    private EndpointDeclaration logStreamSocket() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.GET)
                .endpointUri("log/channel")
                .handler(ctx -> WebSockets.websocket(ctx, new WebSocketHandler<String>() {
                    @Override
                    public String onOpen(WebSocket webSocket) throws Exception {
                        log.info("WebSocket opened from: " + ctx.getRequest().getRemoteAddress());
                        return "ok";
                    }

                    @Override
                    public void onClose(WebSocketClose<String> close) throws Exception {
                        log.info("WebSocket closed from: " + ctx.getRequest().getRemoteAddress());
                    }

                    @Override
                    public void onMessage(WebSocketMessage<String> frame) throws Exception {
                        commands.action(frame);
                    }
                })).build();
    }
}
