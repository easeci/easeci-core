package io.easeci.core.extension;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Prepare helps with reflection mechanism,
 * object from jar file dynamically attached to
 * EaseCI application process. Firstly it is required
 * to build ReflectiveFactory<T> and declare type that
 * will be returned. ReflectiveFactory have to build helps
 * with ReflectiveFactoryBuilder class.
 * */
public class ReflectiveFactory<T> {
    private Class[] argsTypes;
    private Object[] args;
    private String classReference;

    private ReflectiveFactory(Class[] argsTypes, Object[] args, String classReference) {
        this.argsTypes = argsTypes;
        this.args = args;
        this.classReference = classReference;
    }

    /**
     * Creates an object using the reflection mechanism.
     * @return object of type declared in class level.
     * */
    public T instantiate() {
        try {
            Class classDef = Class.forName(classReference);
            Constructor<T> constructor = classDef.getConstructor(argsTypes);
            T instance = constructor.newInstance();
            return instance;
        } catch (ClassNotFoundException exception) {
            exception.printStackTrace();
        } catch (NoSuchMethodException exception) {
            exception.printStackTrace();
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
        } catch (InstantiationException exception) {
            exception.printStackTrace();
        } catch (InvocationTargetException exception) {
            exception.printStackTrace();
        }
        throw new RuntimeException();
    }

    public static class ReflectiveFactoryBuilder<T> {
        private Class[] argsTypes;
        private Object[] args;
        private String classReference;

        public ReflectiveFactoryBuilder<T> argsTypes(Class[] argsTypes) {
            this.argsTypes = argsTypes;
            return this;
        }

        public ReflectiveFactoryBuilder<T> args(Object[] args) {
            this.args = args;
            return this;
        }

        public ReflectiveFactoryBuilder<T> classReference(String classReference) {
            this.classReference = classReference;
            return this;
        }

        public ReflectiveFactory<T> build() {
            return new ReflectiveFactory<>(argsTypes, args, classReference);
        }
    }
}
