package io.easeci.extension;

import io.easeci.extension.command.PluginDirective;

import java.util.List;

public interface PluginApi {

    List<PluginDirective> directivesExposed();
}
