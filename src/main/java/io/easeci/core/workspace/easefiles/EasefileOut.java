package io.easeci.core.workspace.easefiles;

import lombok.*;

import java.nio.file.Path;

@Data
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(staticName = "of")
public class EasefileOut {
    private EasefileStatus easefileStatus;
    private String easefileContent;
    private String errorMessage;
    private Path filePath;

    public static EasefileOut of(EasefileStatus easefileStatus, String easefileContent, String errorMessage) {
        EasefileOut easefileOut = new EasefileOut();
        easefileOut.easefileStatus = easefileStatus;
        easefileOut.easefileContent = easefileContent;
        easefileOut.errorMessage = errorMessage;
        return easefileOut;
    }
}
