package io.easeci.core.extension;

import java.nio.file.Path;
import java.util.List;

/**
 * @author Karol Meksuła
 * 2020-07-15
 * */
interface PluginInfrastructureInfo {

    /**
     * @return method should return all directories where plugins could be stored
     * */
    List<Path> getPluginDirectories();
}
