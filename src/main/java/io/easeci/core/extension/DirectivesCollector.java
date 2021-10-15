package io.easeci.core.extension;

import io.easeci.extension.command.Directive;

import java.util.List;
import java.util.Optional;

public interface DirectivesCollector {

    List<Directive> collectAll();

    Optional<Directive> find(String directiveName);
}
