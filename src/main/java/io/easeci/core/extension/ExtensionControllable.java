package io.easeci.core.extension;

import io.easeci.api.extension.ActionRequest;
import io.easeci.api.extension.ActionResponse;

public interface ExtensionControllable {

    PluginContainerState state();

    ActionResponse shutdownExtension(ActionRequest actionRequest);

    ActionResponse startupExtension(ActionRequest actionRequest);

    ActionResponse restart(ActionRequest actionRequest);
}
