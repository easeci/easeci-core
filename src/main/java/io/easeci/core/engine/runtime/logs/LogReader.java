package io.easeci.core.engine.runtime.logs;

import io.easeci.api.socket.Commands;
import io.easeci.core.workspace.LocationUtils;
import org.apache.commons.io.input.ReversedLinesFileReader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static io.easeci.api.socket.Commands.LogFetchMode.HEAD;
import static io.easeci.api.socket.Commands.LogFetchMode.TAIL;

public class LogReader {

    public String read(UUID pipelineContextId, long batchSize, int offset, Commands.LogFetchMode mode) {
        if (mode == null) {
            mode = HEAD;
        }
        File logFile = findFile(LocationUtils.getPipelineRunLogLocation(), pipelineContextId);
        String buffered = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(logFile));
            if (HEAD.equals(mode)) {
                if (offset > 0) {
                    long skip = offset * batchSize;
                    for (int i = 0; i < skip; i++) {
                        bufferedReader.readLine();
                    }
                }
                for (int i = 0; i < batchSize; i++) {
                    try {
                        String line = bufferedReader.readLine();
                        if (line == null) {
                            return buffered;
                        } else {
                            buffered = buffered.concat(line).concat("\n");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (TAIL.equals(mode)) {
                ReversedLinesFileReader reversedLinesFileReader;
                try {
                    reversedLinesFileReader = new ReversedLinesFileReader(logFile);
                } catch (IOException e) {
                    e.printStackTrace();
                    return buffered;
                }
                if (offset > 0) {
                    long skip = offset * batchSize;
                    for (int i = 0; i < skip; i++) {
                        reversedLinesFileReader.readLine();
                    }
                }
                String[] arr = new String[Math.toIntExact(batchSize)];
                for (int i = (int) (batchSize - 1); i >= 0; i--) {
                    try {
                        String line = reversedLinesFileReader.readLine();
                        if (line == null) {
                            break;
                        } else {
                            arr[i] = line;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                for (String entry : arr) {
                    if (entry != null) {
                        buffered = buffered.concat(entry).concat("\n");
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffered;
    }

    /**
     * This method reads logs from file, before parameter lastIndex
     * Example:
     * readBefore("8bfbb0ff-6a3f-4b68-998a-86f155b24ae9", 50, 102);
     * So, read logs of pipelineContextId, batch logs size is equals 50.
     * Indexes in range 51-101 will read and return as output of method
     * */
    public List<String> readTail(UUID pipelineContextId, long batchSize) {
        File logFile = findFile(LocationUtils.getPipelineRunLogLocation(), pipelineContextId);
        List<String> buffered = new ArrayList<>();
        if (logFile == null) {
            return buffered;
        }
        ReversedLinesFileReader reversedLinesFileReader;
        try {
            reversedLinesFileReader = new ReversedLinesFileReader(logFile);
        } catch (IOException e) {
            e.printStackTrace();
            return buffered;
        }
        while (true) {
            try {
                String line = reversedLinesFileReader.readLine();
                if (line == null) {
                    break;
                } else {
                    if (buffered.size() < batchSize) {
                        buffered.add(line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Collections.reverse(buffered);
        return buffered;
    }

    public File findFile(Path baseDir, UUID pipelineContextId) {
        final String pipelineContextIdString = pipelineContextId.toString();
        try {
            return Files.find(baseDir, 1, (path, basicFileAttributes) -> path.toString().endsWith(pipelineContextIdString))
                        .findAny()
                        .orElseThrow()
                        .toFile();
        } catch (IOException | NoSuchElementException e) {
            e.printStackTrace();
            return null;
        }
    }
}
