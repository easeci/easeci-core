package com.meksula;

import io.easeci.extension.ExtensionType;
import io.easeci.extension.bootstrap.OnStartup;
import io.easeci.extension.command.PluginDirective;

import java.util.List;

public class WelcomeLogo implements OnStartup {
    @Override
    public void action() {
        final String LOGO =
                        "  ______                   _____ _____                             \n" +
                        " |  ____|                 / ____|_   _|                            \n" +
                        " | |__   __ _ ___  ___   | |      | |      ___ ___  _ __ ___       \n" +
                        " |  __| / _` / __|/ _ \\  | |      | |     / __/ _ \\| '__/ _ \\   \n" +
                        " | |___| (_| \\__ \\  __/  | |____ _| |_   | (_| (_) | | |  __/    \n" +
                        " |______\\__,_|___/\\___|   \\_____|_____|   \\___\\___/|_|  \\___|\n" +
                        " ~ developed by Karol Meksuła 2020                                 \n" +
                        "\n";
        System.out.println(LOGO);
    }

    @Override
    public String about() {
        return null;
    }

    @Override
    public ExtensionType type() {
        return null;
    }

    @Override
    public List<PluginDirective> directivesExposed() {
        return null;
    }
}
