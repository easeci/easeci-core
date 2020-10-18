package io.easeci.core.engine.easefile.loader;

import java.nio.file.Path;

public class LiveLoader implements EasefileLoader {
    private Path localStoragePath;
    private String encodedEasefileContent;

    public static LiveLoader of(String localStoragePath, String encodedEasefileContent) {
        LiveLoader liveLoader = new LiveLoader();
        liveLoader.localStoragePath = Path.of(localStoragePath);
        liveLoader.encodedEasefileContent = encodedEasefileContent;
        return liveLoader;
    }

    @Override
    public String provide() {
//        1. Zapisz sobie backup tego pliku w jakimś katalogu /tmp
//        2. Załaduj, odkoduj i zwróć treść
        return null;
    }
}
