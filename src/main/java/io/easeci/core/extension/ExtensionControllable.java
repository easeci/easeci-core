package io.easeci.core.extension;

import io.easeci.api.extension.ActionRequest;
import io.easeci.api.extension.ActionResponse;

/**
 * Central point of plugins workflow.
 * Thanks to this object you can make common operation in plugins.
 * Use this to manage plugin or check state of it.
 * @author Karol Meksuła
 * 2020-04-21
 * */
public interface ExtensionControllable {

    /**
     * Check state of PluginContainer, receive information about
     * working or not plugins, time when was enabled etc.
     * @return PluginContainerState that is POJO representation
     *         of PluginContainer - main object that holds whole
     *         plugins configuration, state etc.
     * */
    PluginContainerState state();

    /**
     * Use this method to shutdown some plugin. Notice that this plugin
     * will be not removed from workspace but only disabled.
     * You can run plugin again when you want.
     * @param actionRequest is input object that must contains information
     *                      required for make expected action.
     * @return ActionResponse that contains result of method invocation
     * */
    ActionResponse shutdownExtension(ActionRequest actionRequest);

    /**
     * Use this method to startup extension that was shouted down before.
     * If plugin is just enabled it cannot run new process - nothing will happen.
     * @param actionRequest is input object that must contains information
     *                      required for make expected action.
     * @return ActionResponse that contains result of method invocation
     * */
    ActionResponse startupExtension(ActionRequest actionRequest);

    /**
     * Use this method if you want to make both shutdown and startup some plugin.
     * @param actionRequest is input object that must contains information
     *                      required for make expected action.
     * @return ActionResponse that contains result of method invocation
     * */
    ActionResponse restart(ActionRequest actionRequest);
}
