package commons;

import io.easeci.utils.io.YamlUtils;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

public class WorkspaceTestUtils {

    public static Map<?, ?> loadYamlFromResources(String reference) {
        URL url = WorkspaceTestUtils.class.getClassLoader().getResource(reference);
        Path path = Paths.get(Objects.requireNonNull(url).getPath());
        return YamlUtils.ymlLoad(path);
    }

    public static Path buildPathFromResources(String reference) {
        URL url = WorkspaceTestUtils.class.getClassLoader().getResource(reference);
        return Paths.get(Objects.requireNonNull(url).getPath());
    }
}
