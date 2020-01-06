package io.easeci;

import io.easeci.interpreter.Python;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EaseciCoreApplication {

    public static void main(String[] args) {
        Python.initializeInterpreter();
        SpringApplication.run(EaseciCoreApplication.class, args);
    }

}
