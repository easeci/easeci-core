package io.easeci.core.engine.pipeline;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class Key {
    private KeyType keyType;

    public enum KeyType {
        PIPELINE
    }
}
