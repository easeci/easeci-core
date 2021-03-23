package io.easeci.core.engine.easefile.parser.parts;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Utils {

    public static String propertyToList(List<Line> lines) {
        return lines.subList(1, lines.size())
                .stream()
                .map(Line::getContent)
                .collect(Collectors.joining("\n"));
    }

    public static Optional<Line> findLineStartingWith(List<Line> lines, String startsPhrase) {
        return lines.stream()
                    .filter(line -> line.getContent().trim().startsWith(startsPhrase))
                    .findFirst();
    }
}
