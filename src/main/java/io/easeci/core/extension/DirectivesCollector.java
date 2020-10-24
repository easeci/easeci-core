package io.easeci.core.extension;

import io.easeci.extension.command.Directive;

import java.util.List;

public interface DirectivesCollector {

    List<Directive> collectAll();
}
