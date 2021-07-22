package io.easeci.api.socket.log.dto;

import lombok.Data;

@Data
public class IncomingLogEntry {
    private String title;
    private String content;
    private String header;
    private long timestamp;
}
