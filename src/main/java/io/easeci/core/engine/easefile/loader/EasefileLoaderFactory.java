package io.easeci.core.engine.easefile.loader;

import io.easeci.api.parsing.RunParseProcess;

import static java.util.Objects.isNull;

public class EasefileLoaderFactory {

    public static EasefileLoader factorize(RunParseProcess parseProcess) {
        if (isNull(parseProcess) || isNull(parseProcess.getSource())) {
            throw new IllegalArgumentException("Cannot infer EasefileLoader because source is null");
        }
        switch (parseProcess.getSource()) {
            case LIVE:
                return LiveLoader.of(parseProcess.getLocalStoragePath(), parseProcess.getEncodedEasefileContent());
            case EASEFILE:
                return WorkspaceLoader.of(parseProcess.getLocalStoragePath());
            case GIT_REPOSITORY:
                return GitLoader.of(parseProcess.getGitRepositoryUrl());
            default:
                throw new IllegalArgumentException("Cannot infer EasefileLoader from source: " + parseProcess.getSource());
        }
    }
}
