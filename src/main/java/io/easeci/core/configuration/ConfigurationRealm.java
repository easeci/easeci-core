package io.easeci.core.configuration;

public interface ConfigurationRealm {
    ConfigurationFile retrieve(Class<?> configurationClass);
}
