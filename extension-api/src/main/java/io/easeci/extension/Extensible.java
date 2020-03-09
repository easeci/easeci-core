package io.easeci.extension;

public interface Extensible<T, U> {
   T extend(U extension);
}
