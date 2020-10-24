package io.easeci.extension.command;

import io.easeci.extension.directive.CodeProvider;

import java.util.List;

/**
 * Ready and complete interface for Plugin that wants to
 * expose function for provide chunk of code that
 * will be embed in complete result executable file on server.
 * @author Karol Meksuła
 * 2020-10-24
 * */
public interface Directive extends CodeProvider {

    /**
     * Supply directive name.
     * @return string representation of directive name
     *          for example: $git, $maven, $aws etc.
     * */
    String getDirectiveName();

    /**
     * Supply list of available commands exposed by plugin.
     * @return list of commands that plugin implemented this interface exposed.
     * */
    List<Command> getAvailableCommandList() throws IllegalAccessException;
}
