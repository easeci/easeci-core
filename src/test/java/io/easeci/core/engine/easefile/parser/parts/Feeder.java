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
                        "    environment: 'easeci-java-17'\n" +
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

    public static Supplier<List<Line>> provideCorrectVariables() {
        return () ->
                wrapLines("variables:\n" +
                        "    _repo_address: 'https://github.com/easeci/easeci-core'\n" +
                        "    _repo_clone_target: '/var/sources/easeci'\n" +
                        "    _artifactory_url: 'https://easeci-artifactory.io'\n" +
                        "    _height: 1.77\n" +
                        "    _weight: 68,9\n" +
                        "    _age: 27\n" +
                        "    _planck: 6.62607004\n" +
                        "    _decimal: 0436.000000000000000000000000062607004\n" +
                        "    _long_num: 0436453000000000000000000000000062607004\n" +
                        "    _dev_hosts:\n" +
                        "        - '127.0.0.1'\n" +
                        "        - '127.0.0.2'\n" +
                        "        - '127.0.0.3'\n" +
                        "        - '127.0.0.4'\n" +
                        "        - '127.0.0.5'\n" +
                        "        - '127.0.0.6'");
    }

    public static Supplier<List<Line>> provideCorrectVariables2() {
        return () ->
                wrapLines("variables:");
    }

    public static Supplier<List<Line>> provideCorrectVariables3() {
        return () ->
                wrapLines("variables:\n" +
                        "    _nested_object:\n" +
                        "       human:\n" +
                        "          _height: 1.77\n" +
                        "          _weight: 68,9\n" +
                        "          _age: 27\n" +
                        "          _friends:\n" +
                        "             - 'John'\n" +
                        "             - 'Thomas'\n" +
                        "             - 'Marry'\n");
    }

    public static Supplier<List<Line>> provideCorrectVariables4() {
        return () ->
                wrapLines("variables:\n" +
                        "    _nested_object\n" +
                        "       human:\n" +
                        "          _height: 1.77\n" +
                        "          _weight: 68,9\n" +
                        "          _age: 27\n" +
                        "          _friends:\n" +
                        "             - 'John'\n" +
                        "             - 'Thomas'\n" +
                        "             - 'Marry'\n");
    }

    public static Supplier<List<Line>> provideCorrectFlow() {
        return () ->
                wrapLines("flow:\n" +
                        "   -\n" +
                        "       stage_name: 'Prepare building environment'\n" +
                        "       steps:\n" +
                        "           - $ssh mkdir -p {_repo_clone_target}\n" +
                        "   -\n" +
                        "       stage_name: 'Preparation of project building'\n" +
                        "       steps:\n" +
                        "           - $git clone {_repo_address}\n" +
                        "   -\n" +
                        "       stage_name: 'Unit tests'\n" +
                        "       steps:\n" +
                        "           - $mvn test\n" +
                        "           - $bash cp -r target/test-result/* /tmp/logs/\n" +
                        "   -\n" +
                        "       stage_name: 'Building project'\n" +
                        "       steps:\n" +
                        "           - $mvn install\n" +
                        "   -\n" +
                        "       stage_name: 'Publish artifact'\n" +
                        "       steps:\n" +
                        "           - $artifactory {_repo_clone_target} {_artifactory_url}\n" +
                        "   -\n" +
                        "       stage_name: 'Deploy to development env'\n" +
                        "       steps:\n" +
                        "           - $deploy ssh {_dev_hosts}\n" +
                        "   -\n" +
                        "       stage_name: 'Deploy to production env'\n" +
                        "       stage_variables:\n" +
                        "           log_dir: /tmp/logs/\n" +
                        "       steps: \n" +
                        "           - |- \n" +
                        "                $bash\n" +
                        "                   echo 'This is multiline bash script'\n" +
                        "                   cp -r target/test-result/* {log_dir}\n" +
                        "                   echo 'End of script'\n" +
                        "           - |- \n" +
                        "                $bash\n" +
                        "                   echo 'This is second multiline bash script'\n" +
                        "                   cp -r target/test-result/* {log_dir}\n" +
                        "                   echo 'End of script'\n"
                );
    }
}

