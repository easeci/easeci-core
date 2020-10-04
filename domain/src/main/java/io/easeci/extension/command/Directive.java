package io.easeci.extension.command;

import java.util.List;

public interface Directive {

    String getDirectiveName();

    List<Command> getCommandList();
}
