package io.easeci.api.parsing;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.easeci.api.validation.ApiRequestValidator;
import io.easeci.api.validation.ValidationException;
import io.easeci.core.engine.easefile.loader.EasefileContentMalformed;
import io.easeci.core.engine.easefile.loader.EasefileLoaderFactory;
import io.easeci.core.engine.easefile.parser.EasefileParser;
import io.easeci.core.engine.easefile.parser.MainEasefileParser;
import io.easeci.core.workspace.projects.ProjectManager;
import io.easeci.server.EndpointDeclaration;
import io.easeci.server.InternalHandlers;
import org.eclipse.jgit.api.errors.GitAPIException;
import ratpack.http.HttpMethod;

import java.io.IOException;
import java.util.List;

import static io.easeci.api.validation.ApiRequestValidator.extractBody;
import static ratpack.http.MediaType.APPLICATION_JSON;

public class EasefileParsingHandlers implements InternalHandlers {
    private final static String MAPPING = "parse";
    private ObjectMapper objectMapper;
    private EasefileParser easefileParser;

    public EasefileParsingHandlers() {
        this.objectMapper = new ObjectMapper();
        this.easefileParser = new MainEasefileParser(ProjectManager.getInstance());
    }

    @Override
    public List<EndpointDeclaration> endpoints() {
        return List.of(makePipeline());
    }

    // Create Pipeline from Easefile
    private EndpointDeclaration makePipeline() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.POST)
                .endpointUri(MAPPING)
                .handler(ctx -> extractBody(ctx.getRequest(), RunParseProcess.class)
                        .map(runParseProcess -> {
                            String easefilePlainContent = EasefileLoaderFactory.factorize(runParseProcess).provide();
                            return easefileParser.parse(easefilePlainContent);
                        }).map(ParseProcessResponse::of)
                        .mapError(this::errorMapping)
                        .map(parseProcessResponse -> objectMapper.writeValueAsBytes(parseProcessResponse))
                        .mapError(ApiRequestValidator::handleException)
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    private ParseProcessResponse errorMapping(Throwable throwable) {
        if (throwable instanceof ValidationException) {
            throw (ValidationException) throwable;
        }
        if (throwable instanceof IOException) {
            return ParseProcessResponse.withError("Cannot load Easefile from defined source");
        }
        if (throwable instanceof IllegalAccessException) {
            return ParseProcessResponse.withError(throwable.getMessage());
        }
        if (throwable instanceof GitAPIException) {
            return ParseProcessResponse.withError("Error occurred while deal with remote git repository. Exception message: " + throwable.getMessage());
        }
        if (throwable instanceof EasefileContentMalformed) {
            return ParseProcessResponse.withError(throwable.getMessage());
        }
        return ParseProcessResponse.withError("Some unrecognized error occurred while trying to parse Easefile");
    }

    // Make only static analyse to check your Easefile's content
    private EndpointDeclaration staticAnalise() {
        return null;
    }
}
