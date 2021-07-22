package io.easeci.core.engine.runtime.logs;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    static Consumer<String> simplePrintingLogConsumer() {
        return System.out::println;
    }

    static int extractIndex(String entryLog) {
        Pattern pattern = Pattern.compile("index=[0-9]+,");
        Matcher matcher = pattern.matcher(entryLog);
        if (matcher.find()) {
            String result = matcher.group();
            return Integer.parseInt(result.substring(6, result.length() - 1));
        }
        return 0;
    }

    static List<String> oneStringToLines(String entryLog) {
        return Arrays.asList(entryLog.split("\n"));
    }
}
