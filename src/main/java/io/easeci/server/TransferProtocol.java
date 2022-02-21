package io.easeci.server;

public enum TransferProtocol {
    HTTP {
        @Override
        public String prefix() {
            return "http://";
        }
    },
    HTTPS {
        @Override
        public String prefix() {
            return "https://";
        }
    };

    public abstract String prefix();
}
