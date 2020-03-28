package io.easeci.core.extension;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.jar.Attributes;

import static java.util.Objects.nonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class ExtensionManifest {
    final static String IMPLEMENTS = "Implements";
    final static String ENTRY_CLASS = "Entry-Class";
    final static String EXTENSION_PACKAGE_PREFIX = "io.easeci.extension";

    @Getter
    private String implementsProperty;

    @Getter
    private String entryClassProperty;

    static ExtensionManifest of(String implementsProperty, String entryClassProperty) {
        return new ExtensionManifest(implementsProperty, entryClassProperty);
    }

    static ExtensionManifest of(Attributes attributes) {
        String implementsProperty = attributes.getValue(IMPLEMENTS);
        String entryClassProperty = attributes.getValue(ENTRY_CLASS);
        return new ExtensionManifest(implementsProperty, entryClassProperty);
    }

    boolean isComplete() {
        return nonNull(implementsProperty)
                && nonNull(entryClassProperty)
                && implementsProperty.startsWith(EXTENSION_PACKAGE_PREFIX)
                && !entryClassProperty.isEmpty();
    }
}

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
class ExtensionManifestException extends RuntimeException {
    private String message;

    @Override
    public String getMessage() {
        return this.message;
    }
}