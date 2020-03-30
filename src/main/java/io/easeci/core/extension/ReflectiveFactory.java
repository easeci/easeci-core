package io.easeci.core.extension;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;

import static java.util.Objects.isNull;

/**
 * Prepare helps with reflection mechanism,
 * object from jar file dynamically attached to
 * EaseCI application process. Firstly it is required
 * to build ReflectiveFactory<T> and declare type that
 * will be returned. ReflectiveFactory have to build helps
 * with ReflectiveFactoryBuilder class.
 * */
class ReflectiveFactory<T> {
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
    @SuppressWarnings("unchecked")
    T instantiate(Plugin.JarArchive jarArchive) {
        try {
            URLClassLoader classLoader = new URLClassLoader(new URL[]{jarArchive.getJarUrl()}, Thread.currentThread().getContextClassLoader());
            Class<?> myClass = Class.forName(jarArchive.getExtensionManifest().getEntryClassProperty(), true, classLoader);
            return (T) myClass.getConstructor(new Class[]{}).newInstance(new Object[]{});
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

    static class ReflectiveFactoryBuilder<T> {
        private Class[] argsTypes;
        private Object[] args;
        private String classReference;

        ReflectiveFactoryBuilder<T> argsTypes(Class[] argsTypes) {
            this.argsTypes = argsTypes;
            return this;
        }

        ReflectiveFactoryBuilder<T> args(Object[] args) {
            this.args = args;
            return this;
        }

        ReflectiveFactoryBuilder<T> classReference(String classReference) {
            this.classReference = classReference;
            return this;
        }

        ReflectiveFactory<T> build() {
            if (isNull(classReference) || classReference.isEmpty()) {
                throw new IllegalStateException("classReference field indicates the class that should be created. It cannot be null or empty!");
            }
            if (isNull(argsTypes)) {
                this.argsTypes = new Class[] {};
            }
            if (isNull(args)) {
                this.args = new Object[] {};
            }
            return new ReflectiveFactory<>(argsTypes, args, classReference);
        }
    }
}
