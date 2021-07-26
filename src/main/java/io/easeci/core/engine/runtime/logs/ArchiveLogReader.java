package io.easeci.core.engine.runtime.logs;

import io.easeci.api.socket.Commands;

import java.util.UUID;

/**
 * Interface that represents access object for read archive logs.
 * @author Karol Meksuła
 * 2021-07-26
 * */
public interface ArchiveLogReader {

    /**
     * @param batchSize size of one package of logs per method invoke
     * @param offset indicates which package you need to read
     * @param mode read content of file from head or from tail
     * @return string value of list of LogEntry.class objects
     * */
    String getArchiveFileLogRail(UUID pipelineContextId, long batchSize, int offset, Commands.LogFetchMode mode);
}
