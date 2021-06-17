package io.easeci.core.engine.runtime.logs;

import java.util.function.Consumer;

public class Utils {

    static Consumer<String> simplePrintingLogConsumer() {
        return System.out::println;
    }
}
