package io.easeci.api.projects;

public enum ProjectDomainStatus {
    PROJECT_GROUP_CREATED {
        @Override
        public String message() {
            return "Project group created with success";
        }
    },
    PROJECT_GROUP_REMOVED {
        @Override
        public String message() {
            return "Project group removed with success";
        }
    },
    PROJECT_GROUP_MODIFIED {
        @Override
        public String message() {
            return "Project group modified with success";
        }
    };

    public abstract String message();
}
