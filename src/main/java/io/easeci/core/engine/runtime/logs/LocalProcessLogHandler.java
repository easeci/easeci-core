package io.easeci.core.engine.runtime.logs;

import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.LogOutputStream;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class LocalProcessLogHandler implements PipelineRuntimeLogHandler {

    public void handle() throws InterruptedException, TimeoutException, IOException {
        new ProcessExecutor().command("docker", "logs", "fc635b8829c6", "-f")
                .redirectOutput(new LogOutputStream() {
                    @Override
                    protected void processLine(String line) {
                        System.out.println(line);
                    }
                })
                .execute();
    }
}
