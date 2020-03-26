package io.easeci;

import io.easeci.core.bootstrap.BootstrapperFactory;
import io.easeci.core.extension.ExtensionsManager;
import io.easeci.extension.Extensible;
import io.easeci.extension.bootstrap.OnStartup;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

@SpringBootApplication
public class EaseciCoreApplication {

    public static void main(String[] args) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException, ClassNotFoundException, InstantiationException {
        BootstrapperFactory.factorize().bootstrap(args);

        ExtensionsManager.getInstance().parsePluginFile();

//        Skanujemy w poszukiwaniu pluginów
        File file = new File("/home/karol/dev/java/easeci-core-java/welcome-logo/build/libs/welcome-logo-0.0.1.jar");
        URL url = file.toURI().toURL();
        URLClassLoader sysLoader = new URLClassLoader(new URL[0]);

//        Ładowanie pluginów
        Method sysMethod = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
        sysMethod.setAccessible(true);
        sysMethod.invoke(sysLoader, new Object[]{url});

//        Odczytanie manifestu
        JarFile jarFile = new JarFile("/home/karol/dev/java/easeci-core-java/welcome-logo/build/libs/welcome-logo-0.0.1.jar");
        Manifest manifest = jarFile.getManifest();
        Attributes mainAttributes = manifest.getMainAttributes();
        String value = mainAttributes.getValue("Implements");
        String entryClass = mainAttributes.getValue("Entry-Class");
        System.out.println(value);

//        Utworzenie instancji obiektu (generyczna implementacja już jest)
        URLClassLoader classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()}, Thread.currentThread().getContextClassLoader());
        Class<?> myClass = Class.forName(entryClass, true, classLoader);
        OnStartup onStartup = (OnStartup) myClass.getConstructor(new Class[]{}).newInstance(new Object[]{});

//        Kontener, który przechowuje referencje do implementacji pluginów
        Map<String, Extensible> pluginReferenceImplementations = new LinkedHashMap<>();
        pluginReferenceImplementations.put(value, onStartup);

//        Metoda kliencka, pobieram sobie do użycia implementację jaką chcę
        OnStartup onStartup1 = (OnStartup) pluginReferenceImplementations.get("io.easeci.extension.bootstrap.OnStartup");

        /*
        * Problemy do rozwiązania:
        *  - różne implementacje jednego interfejsu - mapa może przechowywać tylko jeden klucz o danej wartości
        *  - włączanie / wyłączanie pluginów
        *
        * */

        onStartup1.action();

        try {
            onStartup1.about();
        } catch (AbstractMethodError e){
            System.out.println("Err");
        }

        SpringApplication.run(EaseciCoreApplication.class, args);
    }
}