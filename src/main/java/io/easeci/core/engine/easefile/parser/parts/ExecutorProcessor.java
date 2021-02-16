package io.easeci.core.engine.easefile.parser.parts;

import io.easeci.core.engine.easefile.parser.analyse.SyntaxError;
import io.easeci.core.engine.pipeline.Executor;
import io.easeci.extension.command.VariableType;
import io.vavr.Tuple;
import io.vavr.Tuple2;

import java.util.*;
import java.util.function.Supplier;

import static io.easeci.core.node.NodeUtils.nodeUuid;

public class ExecutorProcessor implements PipelinePartProcessor<List<Executor>> {

    private static final String LABEL_MAIN = "executor:";
    private static final String AUTO_ASSIGN = "auto";
    private static final Map<String, VariableType> PROPERTIES = new HashMap<>() {{
        put("names", VariableType.LIST);
        put("name", VariableType.STRING);
    }};

    private static Set<String> propertiesNames() {
        return PROPERTIES.keySet();
    }

    @Override
    public Tuple2<Optional<List<Executor>>, List<SyntaxError>> process(Supplier<List<Line>> easefilePartSupplier) {
        final List<Line> lines = easefilePartSupplier.get();
        final List<Executor> executorList = new ArrayList<>(0);
        final List<SyntaxError> syntaxErrors = new ArrayList<>(0);

        for (Line line : lines) {
            final String content = line.getContent();
            if (content.startsWith(LABEL_MAIN)) {
                String[] parts = content.split(LABEL_MAIN);
                if (parts.length > 1) {
                    String oneLineExecutor = parts[1].trim();
                    if (oneLineExecutor.equals(AUTO_ASSIGN)) {
                        executorList.add(createAuto());
                    }
                    else {
                        syntaxErrors.add(SyntaxError.builder()
                                .title("Not expected character after '" + LABEL_MAIN + "' expression")
                                .info("Cannot recognize '" + oneLineExecutor + "' expression, invalid syntax")
                                .lineNumber(line.getLineNumber())
                                .build());
                    }
                }
            } else {
                /*
                * Flow będzie takie:
                * 1. Jeżeli taka property istnieje na liście dla danej sekcji np. executor to:
                *    a) Wywołaj fabrykę i niech stworzy odpowiedni obiekt dziedziczący po DataStructureConstructor
                *    b) Dorzacaj za pomocą buffer() do tego obiektu linie, aż DataStructureConstructor uzna, że została stworzona
                *       kolekcja
                *    c) Wyciągnij strukturę danych i zrób z niej to co chcesz
                *    d) Może zrobić hooka na tym obiekcie, w którym teraz jesteś, aby DataStructureConstructor powiadamiał go
                *       jak będzie miał gotowo utworzony obiekt
                *    e) Może też zwrócić błędy, które bezpośrednio możemy zwracać dalej
                * */


                if (line.getContent().contains(":")) {
                    String[] property = line.getContent().split(":");
                    if (propertiesNames().contains(property[0])) {


                    }
                }
            }
        }
        return Tuple.of(Optional.of(executorList), syntaxErrors);
    }

    private Executor createAuto() {
        return Executor.of(nodeUuid());
    }
}
