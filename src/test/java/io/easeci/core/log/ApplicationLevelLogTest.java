package io.easeci.core.log;

import io.easeci.BaseWorkspaceContextTest;
import io.easeci.core.log.file.EventUtils;
import io.easeci.core.output.Event;
import io.easeci.core.workspace.LocationUtils;
import io.easeci.commons.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationLevelLogTest extends BaseWorkspaceContextTest {

    @Test
    @DisplayName("Should correctly instantiate singleton of ApplicationLevelLog")
    void instantiateTest() {
        ApplicationLevelLog applicationLevelLog = ApplicationLevelLog.getInstance();
        ApplicationLevelLog applicationLevelLog2 = ApplicationLevelLog.getInstance();

        assertNotNull(applicationLevelLog);
        assertEquals(applicationLevelLog, applicationLevelLog2);
    }

    @Test
    @DisplayName("Should correctly handle event by main method of ApplicationLevelLog's instance")
    void handleTest() {
        ApplicationLevelLog applicationLevelLog = ApplicationLevelLog.getInstance();

        Event event = EventUtils.provideEvent();
        applicationLevelLog.handle(event);

        String content = loadAndCleanup(applicationLevelLog.getCurrentLogfile());

        applicationLevelLog.shutdownLogManager();
        assertTrue(EventUtils.EVENT_BYTE_SIZE <= content.getBytes().length);
    }

    @Test
    @DisplayName("Should correctly init log file")
    void initLogFileTest() {
        ApplicationLevelLog applicationLevelLog = ApplicationLevelLog.getInstance();

        Path path = applicationLevelLog.initLogFile();

        String workspaceLocation = LocationUtils.getWorkspaceLocation();

        assertTrue(path.toString().contains(workspaceLocation));

        FileUtils.fileDelete(path.toString());
    }

    String loadAndCleanup(Path path) {
        String content = FileUtils.fileLoad(path.toString());
        FileUtils.fileDelete(path.toString());
        return content;
    }
}