package io.easeci.core.engine.easefile.loader;

public class GitLoader implements EasefileLoader {
    private String gitRepositoryUrl;
    private String plainEasefileContent;
    private String localStoragePath;

    public static EasefileLoader of(String gitRepositoryUrl) {
        GitLoader easefileLoader = new GitLoader();
        easefileLoader.gitRepositoryUrl = gitRepositoryUrl;
        return easefileLoader;
    }

    @Override
    public String provide() {
//        podaj repo gita
//        pobierz sobie na lokalny storage
        return null;
    }
}
