package io.easeci.server;

import io.easeci.core.node.connect.ClusterInformation;

/**
 * This enum is about communication type between servers.
 * A values of this enum define the way how the URL to master will be created.
 * */
public enum CommunicationType {

    /**
     * Communication via domain name for example: easeci.com
     * */
    DOMAIN {
        @Override
        public String urlBase(ClusterInformation clusterInformation) {
            return clusterInformation.domainName();
        }
    },
    /**
     * Communication via domain name and port for example: easeci.com:8000
     * */
    DOMAIN_WITH_PORT {
        @Override
        public String urlBase(ClusterInformation clusterInformation) {
            return clusterInformation.domainName().concat(":").concat(clusterInformation.port());
        }
    },
    /**
     * Communication via IPv4 with port for example: 12.43.156.43:9000
     * */
    IP_WITH_PORT {
        @Override
        public String urlBase(ClusterInformation clusterInformation) {
            return clusterInformation.ip().concat(":").concat(clusterInformation.port());
        }
    },
    /**
     * Simple communication via IPv4 for example: 12.43.156.43
     * */
    IP {
        @Override
        public String urlBase(ClusterInformation clusterInformation) {
            return clusterInformation.ip();
        }
    };

    /**
     * Should return for example:
     * 129.44.35.192:8080
     * 129.44.35.192
     * or simple domain name:
     * easeci.com
     * easeci.com:8000
     * */
    public abstract String urlBase(ClusterInformation clusterInformation);
}
