package io.easeci.extension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum ExtensionType {
    STANDALONE_PLUGIN,
    EXTENSION_PLUGIN,
    DIRECTIVE_PLUGIN;

    private static final Map<ExtensionType, String> extensionTypeInterfaceMap = new HashMap<>() {{
       put(STANDALONE_PLUGIN, "io.easeci.extension.Standalone");
       put(EXTENSION_PLUGIN, "io.easeci.extension.bootstrap.OnStartup");
       put(DIRECTIVE_PLUGIN, "io.easeci.extension.command.Directive");
    }};

    private static final Map<String, ExtensionType> interfaceExtensionTypeMap = new HashMap<>() {{
       put("io.easeci.extension.Standalone", STANDALONE_PLUGIN);
       put("io.easeci.extension.bootstrap.OnStartup", EXTENSION_PLUGIN);
       put("io.easeci.extension.command.Directive", DIRECTIVE_PLUGIN);
    }};

    public static String toInterface(ExtensionType extensionType) {
        return Optional.ofNullable(extensionTypeInterfaceMap.get(extensionType))
                .orElse(extensionType.toString());
    }

    public static ExtensionType toEnum(String interfaceName) {
        return Optional.ofNullable(interfaceExtensionTypeMap.get(interfaceName))
                .orElse(null);
    }
}
