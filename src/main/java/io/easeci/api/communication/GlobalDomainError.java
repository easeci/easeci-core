package io.easeci.api.communication;

public enum GlobalDomainError {
    UNEXPECTED_SERVER_ERROR {
        @Override
        public String message() {
            return "Unexpected error occurred while request processing";
        }
    },
    UNEXPECTED_SUCCESS {
        @Override
        public String message() {
            return "Seems to request ends with success but some additional actions may not went successfully";
        }
    };

    public abstract String message();
}
