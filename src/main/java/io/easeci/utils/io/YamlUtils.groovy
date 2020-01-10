package io.easeci.utils.io;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.file.Path;

class YamlUtils {

    static Map<String, ?> ymlLoad(Path path) {
        return null;
    }

    static String ymlGetParam(Path path, String refs) {
        return null;
    }

    static Map<String, ?> ymlCreate(Path path, Map<String, ?> mapAsYml) {
        return null;
    }

    static String ymlAppend(Path path, YmlField ymlField) {
        return null;
    }

    static String ymlChange(Path path, Object value) {
        return null;
    }

    static String ymlDelete(Path path, String refs) {
        return null;
    }

    @Getter
    @AllArgsConstructor
    static class YmlField<T> {
        private String referenceKey;
        private T value;
    }
}
