package io.easeci.core.engine.runtime.logs;

import lombok.*;

import java.io.Serializable;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LogEntry implements Serializable {
    private long index;
    private String header;
    private String author;
    private long timestamp;
    private String text;
    private EndChar endChar;
    private LogExtent logExtent;
    private Color color;
    private FontWeight fontWeight;
    private FontStyle fontStyle;

    public LogEntry(long index, LogExtent logExtent, String header, String author, long timestamp, String text) {
        this.index = index;
        this.author = author;
        this.timestamp = timestamp;
        this.text = text;
        this.header = header;
        this.logExtent = logExtent;
        this.endChar = EndChar.NEXT_LINE;
        this.color = Color.BLACK;
        this.fontWeight = FontWeight.NORMAL;
        this.fontStyle = FontStyle.NORMAL;
    }

    protected void setIndex(long index) {
        this.index = index;
    }

    public enum EndChar {
        /**
         * After log will be next line character
         * */
        NEXT_LINE,
        /**
         * After log's text will be put space
         * */
        SPACE,
        /**
         * Nothing will be put after text
         * */
        NONE
    }

    public enum LogExtent {
        SYSTEM,
        RUNTIME
    }

    public enum Color {
        RED,
        BLUE,
        GREEN,
        BLACK
    }

    public enum FontWeight {
        NORMAL,
        BOLDER,
        BOLDEST
    }

    public enum FontStyle {
        NORMAL,
        CURSIVE,
        UNDERLINED
    }
}
