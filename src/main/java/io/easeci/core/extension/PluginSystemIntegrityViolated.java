package io.easeci.core.extension;

class PluginSystemIntegrityViolated extends RuntimeException {

    PluginSystemIntegrityViolated() {
        super("Plugin system integrity violated - cannot correlate information from plugins.yml and plugins-config.json! Some plugin's config may be missing!");
    }

    PluginSystemIntegrityViolated(String message) {
        super(message);
    }
}
