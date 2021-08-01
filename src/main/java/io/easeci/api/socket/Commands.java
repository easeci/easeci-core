package io.easeci.api.socket;

import io.easeci.core.engine.runtime.PipelineContextNotExists;
import io.easeci.core.engine.runtime.PipelineContextSystem;
import io.easeci.core.engine.runtime.logs.LogBuffer;
import io.easeci.core.engine.runtime.logs.LogRail;
import lombok.extern.slf4j.Slf4j;
import ratpack.websocket.WebSocket;
import ratpack.websocket.WebSocketMessage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class Commands {

    public enum LogFetchMode {
        HEAD,
        TAIL,
        LIVE
    }

    public void action(WebSocketMessage<String> frame) {
        final String input = frame.getText();
        WebSocket webSocket = frame.getConnection();
        String openResponseCommunicate = "connected";
        if (input.startsWith("stream --pipelineContextId")) {
            LogRail logRail;
            try {
                Optional<String> offset = extractOptionalProperty(input, "--offset");
                UUID pipelineContextId = extractUuid(input);
                logRail = PipelineContextSystem.getInstance().getLogRail(pipelineContextId);
                offset.filter(s -> !s.isEmpty())
                        .map(LogBuffer.Options::valueOf)
                        .ifPresentOrElse(options -> logRail.initPublishing(webSocket::send, options),
                                        () -> logRail.initPublishing(webSocket::send));
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
        // example: read --batch=50 --offset=0 --pipelineContextId=50679665-ad74-4ac5-9701-1b6e2eeea193 --mode=HEAD
        else if (input.startsWith("read")) {
            try {
                long batchSize = Long.parseLong(extractProperty(input, "--batch"));
                int offset = Integer.parseInt(extractProperty(input, "--offset"));
                UUID pipelineContextId = UUID.fromString(extractProperty(input, "--pipelineContextId"));
                LogFetchMode mode = extractMode(input, "--mode");
                log.info("Websocket connected historical log streaming from file for pipelineContextId: {}", pipelineContextId);
                String output = PipelineContextSystem.getInstance().getArchiveFileLogRail(pipelineContextId, batchSize, offset, mode);
                frame.getConnection().send(output);
            }
            catch (PipelineContextNotExists e) {
                log.error(e.getMessage());
            } catch (Throwable t) {
                String msg = "Bad input. Error: " + t.getMessage();
                log.error(msg);
            }
        } else {
            webSocket.send("Command not supported: '" + input + "'");
        }
    }

    private UUID extractUuid(String input) {
        try {
            String[] split = input.split("\\s");
            String potentialUuid = split[1].split("=")[1].trim();
            return UUID.fromString(potentialUuid);
        } catch (Throwable t) {
            log.error("Cannot extract uuid from input");
            return null;
        }
    }

    /**
     * @param propertyKey value to find in command for example: --batch or --offset
     * */
    String extractProperty(String input, String propertyKey) {
        try {
            return List.of(input.split("\\s"))
                    .stream()
                    .filter(property -> property.startsWith(propertyKey))
                    .findFirst()
                    .orElseThrow(() -> new CommandElementNotPresent(input, propertyKey))
                    .split("=")[1];
        } catch (IndexOutOfBoundsException e) {
            log.error("Command '" + propertyKey + "' requires value! You not typed value for example:" +
                    "\n--batch=50 <-- correct\n" +
                    "--batch=   <-- not correct\n" +
                    "--batch = 50   <-- not correct"
            );
            return "";
        } catch (Throwable t) {
            log.error("Cannot retrieve property '{}' from command '{}'", propertyKey, input);
            t.printStackTrace();
            return "";
        }
    }

    Optional<String> extractOptionalProperty(String input, String propertyKey) {
        try {
            return Optional.ofNullable(extractProperty(input, propertyKey));
        } catch (Throwable ex) {
            return Optional.empty();
        }
    }

    private LogFetchMode extractMode(String input, String propertyKey) {
        try {
            String result = extractProperty(input, propertyKey);
            if (result != null) {
                try {
                    return LogFetchMode.valueOf(result);
                } catch (Throwable t) {
                    return LogFetchMode.HEAD;
                }
            }
            return LogFetchMode.HEAD;
        } catch (Throwable t) {
            return LogFetchMode.HEAD;
        }
    }
}
