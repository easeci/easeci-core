package io.easeci.core.extension;

import java.nio.file.Path;
import java.util.List;

/**
 * Main interface that is responsible for creation of
 * whole plugin management infrastructure.
 * @author Karol Meksuła
 * 2020-03-12
 * */
interface InfrastructureInit {

    /**
     * @return method should return all directories where plugins could be stored
     * */
    List<Path> getPluginDirectories();

    /**
     * Reloads/loads/refreshes infrastructure data. Simply defines way
     * how to provide new data abouts paths to location etc.
     * */
    void loadInfrastructure() throws Exception;

    /**
     * Returns status of extensions infrastructure.
     * @return boolean that inform client method about plugin
     *          management system initializing status.
     *          Return 'true' if all defined parts of infrastructure
     *          were created correctly.
     *          Return 'false' when cannot detect correct configuration
     *          of extensions structure.
     * */
    boolean isInitialized();

    /**
     * One method that triggers initializing of plugins/extensions
     * structure required to correct build this point of application.
     * */
    void prepareInfrastructure() throws Exception;
}
