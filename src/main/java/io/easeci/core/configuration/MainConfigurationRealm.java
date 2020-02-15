package io.easeci.core.configuration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static java.util.Objects.isNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MainConfigurationRealm implements ConfigurationRealm {
    private static MainConfigurationRealm realm;

    public static MainConfigurationRealm getInstance() {
        if (isNull(realm)) {
            realm = new MainConfigurationRealm();
        }
        return realm;
    }

    @Override
    public ConfigurationFile retrieve(Class<?> configurationClass) {
        return null;
    }
}
