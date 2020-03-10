package io.easeci.core.extension;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static java.util.Objects.isNull;

@Getter
@AllArgsConstructor
public class Plugin {
    private String name;
    private String version;

    public static Plugin of(String name, String version) {
        if (isNull(name)) {
            throw new RuntimeException("Cannot create Plugin.class instance, name of plugin is null");
        }
        if (isNull(version)) {
            throw new RuntimeException("Cannot create Plugin.class instance, version of plugin is null");
        }
        return new Plugin(name, version);
    }

    @Override
    public String toString() {
        return "~ Plugin '".concat(name)
                .concat("' ver. ")
                .concat(version);
    }
}
