package io.easeci.core.engine.runtime;

import io.easeci.core.engine.runtime.logs.LogRail;
import io.easeci.core.workspace.LocationUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.UUID;

import static io.easeci.core.workspace.LocationUtils.getPipelineRunHistoryLogFileLocation;

@Slf4j
public class PipelineContextHistoryDefault implements PipelineContextHistory {

    public PipelineContextHistoryDefault() {
        initFile();
    }

    @Override
    public boolean saveHistoricalLogEntity(UUID pipelineContextId, UUID pipelineId) {
        PipelineContextHistoryEntity entity = PipelineContextHistoryEntity.builder()
                                                                          .pipelineId(UUID.randomUUID().toString())
                                                                          .pipelineContextId(UUID.randomUUID().toString())
                                                                          .runPipelineDateTime(LocalDateTime.now())
                                                                          .build();
        try {
            Files.write(getPipelineRunHistoryLogFileLocation(), entity.toEntity(), StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Path initFile() {
        if (!Files.exists(getPipelineRunHistoryLogFileLocation())) {
            final Path historyLogDirPath = LocationUtils.getPipelineRunHistoryLogLocation();
            if (Files.notExists(historyLogDirPath)) {
                try {
                    Files.createDirectories(historyLogDirPath);
                } catch (IOException e) {
                    log.info("Could not create directory in path: " + historyLogDirPath);
                    e.printStackTrace();
                }
            }
            try {
                Files.createFile(getPipelineRunHistoryLogFileLocation());
            } catch (IOException e) {
                e.printStackTrace();
            }
            log.info("File for store information about historical pipeline run created: '{}'", getPipelineRunHistoryLogFileLocation());
        }
        log.info("File for store information about historical pipeline just exists here: '{}'", getPipelineRunHistoryLogFileLocation());
        return getPipelineRunHistoryLogFileLocation();
    }

    @Override
    public LogRail findHistoricalLogs(UUID pipelineContextId) {
        return null;
    }
}
