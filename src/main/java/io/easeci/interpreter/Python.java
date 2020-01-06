package io.easeci.interpreter;

import org.python.util.PythonInterpreter;

import java.util.Properties;

public class Python {
    public static final String LANG = "python2.7";

    public static void initializeInterpreter() {
        Properties props = new Properties();
        props.put("python.home","/usr/bin/python");
        props.put("python.console.encoding", "UTF-8");
        props.put("python.security.respectJavaAccessibility", "false");
        props.put("python.import.site","false");
        PythonInterpreter.initialize(System.getProperties(), props, new String[0]);
    }

    public static void execute(String script) {
        try(PythonInterpreter pyInterp = new PythonInterpreter()) {
            pyInterp.exec(script);
        }
    }
}
