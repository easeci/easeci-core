package io.easeci.utils.io;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

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

    public static String ymlInvalidContent() {
        return "main:\n" +
        "   paths:\n" +
        "   temp: /tmp/ease\n" +
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

    public static Map<String, Object> mapContent() {
        return new HashMap<>() {{
            put("main", new HashMap<String, Object>() {{
                put("paths", new HashMap<String, Object>() {{
                    put("temp", "/tmp/ease");
                    put("home", "/usr/local/ease");
                }});
            }});
            put("output", new HashMap<String, Object>() {{
                put("paths", new HashMap<String, Object>() {{
                    put("queue", new HashMap<String, Object>() {{
                        put("max-size", 200);
                    }});
                    put("autopublishing", "False");
                }});
            }});
        }};
    }
}
