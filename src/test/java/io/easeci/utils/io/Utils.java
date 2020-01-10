package io.easeci.utils.io;

import java.nio.file.Path;

public class Utils {

    public static String ymlContent() {
        return "main:\n" +
        "  paths:\n" +
        "    temp: /tmp/ease\n" +
        "    home: /usr/local/ease\n" +
        "\n" +
        "output:\n" +
        "  queue:\n" +
        "    max-size: 100\n" +
        "  autopublishing: False\n";
    }

    public static String ymlContentUpdated() {
        return "main:\n" +
        "\n" +
        "output:\n" +
        "  queue:\n" +
        "    max-size: 500\n" +
        "  autopublishing: True\n";
    }

    public static String ymlRow() {
        return "row: appended";
    }

    public static Path saveSampleFile(Path path) {
        return FileUtils.fileSave(path.toString(), ymlContent(), false);
    }
}
