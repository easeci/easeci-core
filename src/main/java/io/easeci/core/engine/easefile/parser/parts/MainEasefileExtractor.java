package io.easeci.core.engine.easefile.parser.parts;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

@Slf4j
public class MainEasefileExtractor implements EasefileExtractor, MetadataExtractor, KeyExtractor,
                                              VariableExtractor, StageExtractor, ExecutorExtractor {
    private static final int KEY_LINE_POSITION = 0;

    private List<Line> crudeKey;
    private List<Line> crudeMetadata;
    private List<Line> crudeExecutor;
    private List<Line> crudeVariable;
    private List<Line> crudeStage;

    private static final List<String> LABELS = Arrays.asList(
            "executor:",
            "meta:",
            "variables:",
            "flow:"
    );

    @Override
    public void split(String easefileContent) throws PipelinePartCriticalError {
        if (easefileContent == null) {
            throw new PipelinePartCriticalError(Collections.emptyList());
        }

        final Map<String, List<String>> linesByLabel = new HashMap<>();
        String[] lines = easefileContent.split("\n");
        this.crudeKey = Collections.singletonList(Line.of(KEY_LINE_POSITION + 1, lines[0]));

        String lastLineLabel = "";
        for (int i = 1; i < lines.length; i++) {
            final String lineValue = lines[i];
            for (String label : LABELS) {
                if (lineValue.trim().startsWith(label)) {
                    lastLineLabel = label;
                    List<String> labelLines = linesByLabel.get(lastLineLabel);
                    if (isNull(labelLines)) {
                        List<String> collectedLines = new ArrayList<>();
                        linesByLabel.put(label, collectedLines);
                    }
                } else {
                    ofNullable(linesByLabel.get(lastLineLabel))
                            .ifPresent(list -> {
                                if (!list.contains(lineValue)) {
                                    list.add(lineValue);
                                }
                            });
                }
            }
        }

        Map<String, List<Line>> linesIndexed = convert(linesByLabel);
        linesIndexed.values().forEach(list -> list.remove(list.size() - 1));
        this.crudeMetadata = ofNullable(linesIndexed.get("meta:")).orElse(Collections.emptyList());
        this.crudeExecutor = ofNullable(linesIndexed.get("executor:")).orElseThrow(() -> missingEasefilePartException("executor"));
        this.crudeVariable = ofNullable(linesIndexed.get("variables:")).orElse(Collections.emptyList());
        this.crudeStage = ofNullable(linesIndexed.get("flow:")).orElseThrow(() -> missingEasefilePartException("flow"));
    }

    private Map<String, List<Line>> convert(Map<String, List<String>> linesByLabel) {
        final int lineNumber = 1;
        return linesByLabel.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, stringListEntry -> stringListEntry.getValue().stream()
                        .map(content -> Line.of(lineNumber + 1, content))
                        .collect(Collectors.toList())));
    }

    private PipelinePartCriticalError missingEasefilePartException(String missingPart) {
        return new PipelinePartCriticalError(Collections.singletonList(
                ParsingError.of(
                        "Required Easefile part is missing",
                        "Please include " + missingPart + " easefile part and try again",
                        "Cannot find missing " + missingPart + " easefile part. " +
                                "You must define this one on your Easefile"))
        );
    }

    @Override
    public List<Line> fetchCrudeMetadata() throws PipelinePartError {
        if (isNull(this.crudeMetadata)) {
            log.info("Metadata section is not present in Easefile");
            return Collections.emptyList();
        }
        return this.crudeMetadata;
    }

    @Override
    public List<Line> fetchCrudeKey() throws PipelinePartError {
        if (isNull(this.crudeKey)) {
            throw new PipelinePartError(error("Key"));
        }
        return this.crudeKey;
    }

    @Override
    public List<Line> fetchCrudeVariable() throws PipelinePartError {
        if (isNull(this.crudeVariable)) {
            log.info("Global variables per Easefile section is not present in Easefile");
            return Collections.emptyList();
        }
        return this.crudeVariable;
    }

    @Override
    public List<Line> fetchCrudeStage() throws PipelinePartError {
        if (isNull(this.crudeStage)) {
            throw new PipelinePartError(error("Stage"));
        }
        return this.crudeStage;
    }

    @Override
    public List<Line> fetchCrudeExecutor() throws PipelinePartError {
        if (isNull(this.crudeExecutor)) {
            throw new PipelinePartError(error("Executor"));
        }
        return this.crudeExecutor;
    }

    private List<ParsingError> error(String easefilePartName) {
        return Collections.singletonList(
                ParsingError.of(
                        easefilePartName + " Easefile part was not extracted yet",
                        "First of all, use split(...) method, next you will be able to fetch this Easefile part",
                        easefilePartName + " Easefile part was not extracted yet"
                )
        );
    }
}
