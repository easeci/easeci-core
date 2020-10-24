package io.easeci.extension.directive;

import io.easeci.extension.command.Command;

import java.util.List;

/**
 * Common interface for all plugins that stands as 'Directive' (in domain sense).
 * Look at documentation what 'Directive' means in EaseCI system.
 * Each 'Directive' declaration in Easefile starts with '$' character.
 * For example: '$git clone https://.....'
 * @author Karol Meksuła
 * 2020-10-24
 * */
public interface CodeProvider {

    /**
     * Use this method to provide native code for EaseCI engine, for example: bash
     * Take commands in method argument and return chunk of code generated from command list.
     * @param commands is command list POJO object that are passed to plugin.
     *                 These commands are passed to plugin that implements this interface,
     *                 and then plugin take these commands and process.
     * @return CodeChunk that is object that represent chunk of code that could be used for
     *                 embed in final executive script file
     * */
    CodeChunk provideCode(List<Command> commands) throws IllegalAccessException;
}
