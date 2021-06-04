package io.easeci.core.engine.runtime.logs;

import io.easeci.BaseWorkspaceContextTest;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

class LogBufferTest extends BaseWorkspaceContextTest {

    @Test
    void test() {
//        LogBuffer logBuffer = new LogBuffer();
        Queue<String> strings = new LinkedList<>();
        strings.add("A");
        strings.add("B");
        strings.add("C");
        strings.add("D");

        System.out.println(strings.poll());
        System.out.println(strings.poll());
    }
}