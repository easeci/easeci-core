package io.easeci.core.node.connect;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class ClusterTokenGenerator {

    public static Path generateClusterToken(Path secretClusterTokenLocation) {
        if (Files.exists(secretClusterTokenLocation)) {
            log.info("===> Secret cluster token just exists here: {}. Use it if you want to connect EaseCI worker nodes to EaseCI core", secretClusterTokenLocation);
        } else {
            try {
                final Path fileJustCreated = Files.createFile(secretClusterTokenLocation);
                final String token = generateToken();
                final Path completeTokenFile = Files.writeString(fileJustCreated, token);
                log.info("===> Secret cluster token just created and exists here: {}. Use it if you want to connect EaseCI worker nodes to EaseCI core", secretClusterTokenLocation);
                return completeTokenFile;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return secretClusterTokenLocation;
    }

    public static String generateToken() {
        return RandomStringUtils.randomAlphanumeric(68);
    }
}
