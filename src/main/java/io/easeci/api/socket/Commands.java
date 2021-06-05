package io.easeci.api.socket;

import io.easeci.core.engine.runtime.PipelineContextNotExists;
import io.easeci.core.engine.runtime.PipelineContextSystem;
import io.easeci.core.engine.runtime.logs.LogRail;
import lombok.extern.slf4j.Slf4j;
import ratpack.websocket.WebSocket;
import ratpack.websocket.WebSocketMessage;

import java.util.UUID;

@Slf4j
public class Commands {

    public void action(WebSocketMessage<String> frame) {
        final String input = frame.getText();
        WebSocket webSocket = frame.getConnection();
        if (input.startsWith("stream --pipelineContextId")) {
            String openResponseCommunicate = "connected";
            LogRail logRail;
            try {
                UUID pipelineContextId = extractUuid(input);
                logRail = PipelineContextSystem.getInstance().getLogRail(pipelineContextId);
                logRail.onPublish(webSocket::send);
                logRail.initPublishing();
                log.info("Websocket connected log streaming of pipelineContextId: {}", pipelineContextId);
            }
            catch (PipelineContextNotExists e) {
                openResponseCommunicate = e.getMessage();
                log.error(e.getMessage());
            } catch (Throwable t) {
                String msg = "Bad input. Error: " + t.getMessage();
                log.error(msg);
                openResponseCommunicate = msg;
            }
            webSocket.send(openResponseCommunicate);
        }
    }

    private UUID extractUuid(String input) {
        try {
            String[] split = input.split("\\s");
            String potentialUuid = split[2].trim();
            return UUID.fromString(potentialUuid);
        } catch (Throwable t) {
            log.error("Cannot extract uuid from input");
            return null;
        }
    }
}
