package io.easeci.core.engine.easefile.loader;

public class WorkspaceLoader implements EasefileLoader {
    private String localStoragePath;

    public static EasefileLoader of(String localStoragePath) {
        WorkspaceLoader workspaceLoader = new WorkspaceLoader();
        workspaceLoader.localStoragePath = localStoragePath;
        return workspaceLoader;
    }

    @Override
    public String provide() {
//        ładuje normalnie plik z workspace
        return null;
    }
}
