package io.easeci.core.engine.easefile.loader;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.easeci.core.workspace.easefiles.EasefileManager.hasAccessRight;

public class WorkspaceLoader implements EasefileLoader {
    private String localStoragePath;

    public static EasefileLoader of(String localStoragePath) {
        WorkspaceLoader workspaceLoader = new WorkspaceLoader();
        workspaceLoader.localStoragePath = localStoragePath;
        return workspaceLoader;
    }

    @Override
    public String provide() throws IOException, IllegalAccessException {
        Path path = Paths.get(this.localStoragePath);
        if (hasAccessRight(path)) {
            File file = path.toFile();
            return FileUtils.readFileToString(file, "UTF-8");
        }
        throw new IllegalAccessException("Cannot load file out of workspace. Access denied");
    }

    // for test purpose only. No access rights checking
    protected String testProvide() throws IOException {
        Path path = Paths.get(this.localStoragePath);
        File file = path.toFile();
        return FileUtils.readFileToString(file, "UTF-8");
    }
}
