package io.easeci.core.engine.pipeline;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Getter
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class Key {
    private KeyType keyType;

    public enum KeyType {
        PIPELINE
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Key key = (Key) o;
        return keyType == key.keyType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyType);
    }
}
