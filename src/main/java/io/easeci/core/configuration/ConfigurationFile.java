package io.easeci.core.configuration;

import lombok.Getter;

import java.nio.file.Path;

@Getter
public abstract class ConfigurationFile {
    private Path configFileLocalization;
}
