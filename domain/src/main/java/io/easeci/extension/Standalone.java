package io.easeci.extension;

public interface Standalone extends Extensible {

    void start();

    void stop();

    State state();
}
