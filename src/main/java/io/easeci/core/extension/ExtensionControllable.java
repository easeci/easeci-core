package io.easeci.core.extension;

import io.easeci.api.extension.ActionRequest;
import io.easeci.api.extension.ActionResponse;
import io.easeci.extension.ExtensionType;

public interface ExtensionControllable {
    PluginContainerState state(ExtensionType extensionType);

    ActionResponse shutdownExtension(ActionRequest actionRequest);

    ActionResponse startupExtension(ActionRequest actionRequest);

    ActionResponse restart(ActionRequest actionRequest);
}
