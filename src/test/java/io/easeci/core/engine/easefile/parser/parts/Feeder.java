package io.easeci.core.engine.easefile.parser.parts;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static io.easeci.core.engine.easefile.parser.Utils.wrapLines;

/**
 * Class made for testing purposes, that provides
 * parts of Easefile to testing step by step Easefile parser.
 * */
public class Feeder {

    public static Supplier<List<Line>> provideCorrectExecutor1() {
        return () ->
                wrapLines(
                    "executor:\n" +
                    "  strategy: auto\n" +
                    "  names:\n" +
                    "      - \"easefile-node-01-aws\"\n" +
                    "      - \"easefile-node-02-aws\"\n" +
                    "  nodeUuids:\n" +
                    "      - \"16fd18b6-7169-11eb-9439-0242ac130002\"\n" +
                    "      - \"16fd1af0-7169-11eb-9439-0242ac130002\"\n" +
                    "      - \"16fd2158-7169-11eb-9439-0242ac130002\""
                );
    }

    public static Supplier<List<Line>> provideCorrectExecutor2() {
        return () ->
                wrapLines(
                    "executor:\n" +
                    "  names:\n" +
                    "      - \"easefile-node-01-aws\"\n" +
                    "      - \"easefile-node-02-aws\"\n" +
                    "  nodeUuids:\n" +
                    "      - \"16fd18b6-7169-11eb-9439-0242ac130002\"\n" +
                    "      - \"16fd1af0-7169-11eb-9439-0242ac130002\"\n" +
                    "      - \"16fd2158-7169-11eb-9439-0242ac130002\""
                );
    }

    public static Supplier<List<Line>> provideCorrectExecutor3() {
        return () ->
                wrapLines(
                    "executor:\n" +
                    "  strategy: each\n" +
                    "  names:\n" +
                    "      - \"easefile-node-01-aws\"\n" +
                    "      - \"easefile-node-02-aws\"\n"
                );
    }

    public static Supplier<List<Line>> provideCorrectExecutor4() {
        return () ->
                wrapLines(
                    "executor:\n" +
                    "  strategy: each\n" +
                    "  nodeUuids:\n" +
                    "      - \"16fd18b6-7169-11eb-9439-0242ac130002\"\n" +
                    "      - \"16fd1af0-7169-11eb-9439-0242ac130002\"\n" +
                    "      - \"16fd2158-7169-11eb-9439-0242ac130002\""
                );
    }

    public static Supplier<List<Line>> provideCorrectExecutor5() {
        return () ->
                wrapLines(
                    "executor:\n" +
                    "  strategy: one_of\n" +
                    "  nodeUuids:\n" +
                    "      - \"16fd18b6-7169-11eb-9439-0242ac130002\"\n" +
                    "      - \"16fd1af0-7169-11eb-9439-0242ac130002\"\n" +
                    "      - \"16fd2158-7169-11eb-9439-0242ac130002\""
                );
    }

    public static Supplier<List<Line>> provideCorrectExecutor6() {
        return () ->
                wrapLines(
                    "executor:\n" +
                    "  strategy: master"
                );
    }

    public static Supplier<List<Line>> provideCorrectExecutor7() {
        return () ->
                wrapLines(
                        "executor:\n" +
                                "  strategy: one_of"
                );
    }

    public static Supplier<List<Line>> provideCorrectExecutor8() {
        return () ->
                wrapLines(
                        "executor:\n" +
                                "  strategy: each\n" +
                                "  nodeUuids:\n" +
                                "      - \"16fd18b6-7169-11eb-9439-0242ac130002\"\n" +
                                "      - \"16fd1af0-7169-11eb-9439-0242ac130002\"\n" +
                                "      - \"16fd2158-7169-11eb-9439-0242ac130002\"\n" +
                                "  names:\n" +
                                "      - \"easefile-node-01-aws\"\n" +
                                "      - \"easefile-node-02-aws\"\n"
                );
    }

    public static Supplier<List<Line>> provideCorrectExecutor9() {
        return () ->
                wrapLines(
                        "executor:\n" +
                                "  strategy: each\n" +
                                "  nodeUuids:\n" +
                                "      - \"16fd18b6-7169-11eb-9439-0242ac130\"\n" +
                                "      - \"16fd1af0-7169-11eb-9439-0242ac130002\"\n" +
                                "      - \"16fd2158-7169-11eb-9439-0242ac130002\"\n" +
                                "  names:\n" +
                                "      - \"easefile-node-01-aws\"\n" +
                                "      - \"easefile-node-02-aws\"\n"
                );
    }

    public static Supplier<List<Line>> provideCorrectExecutor10() {
        return () ->
                wrapLines(
                        "executor:\n" +
                                "  strategy: one_of\n" +
                                "  nodeUuids:\n"
                );
    }

    public static Supplier<List<Line>> provideCorrectMetadata() {
        return () ->
                wrapLines("meta:\n" +
                        "    projectId: 0\n" +
                        "    tag: 'java maven'\n" +
                        "    name: 'EaseCI Production'\n" +
                        "    description: 'Java project based on Maven, continuous deployment process'");
    }

    public static Supplier<List<Line>> provideCorrectMetadata2() {
        return () ->
                wrapLines("meta:\n" +
                        "    pipelineId: '516f99d0-8a85-11eb-8dcd-0242ac130003'\n" +
                        "    projectId: 133\n" +
                        "    tag: 'java maven'\n" +
                        "    name: 'EaseCI Production'\n" +
                        "    description: 'Java project based on Maven, continuous deployment process'");
    }

    public static Supplier<List<Line>> provideCorrectMetadata3() {
        return () ->
                wrapLines("meta:\n" +
                        "    projectId: 133\n" +
                        "    tag: 'java gradle'\n" +
                        "    tag: 'java maven'\n" +
                        "    name: 'EaseCI Production'\n" +
                        "    description: 'Java project based on Maven, continuous deployment process'");
    }

    public static Supplier<List<Line>> provideCorrectMetadata4() {
        return () ->
                wrapLines("meta:");
    }

    public static Supplier<List<Line>> provideCorrectMetadata5() {
        return Collections::emptyList;
    }

    public static Supplier<List<Line>> provideCorrectMetadata6() {
        return () ->
                wrapLines("meta:\n" +
                        "    projectId: 0\n" +
                        "    tag: 'java gradle'\n" +
                        "    tag: 'java maven'\n" +
                        "    name: 'EaseCI Production'\n" +
                        "    description: 'Java project based on Maven, continuous deployment process'");
    }


}
