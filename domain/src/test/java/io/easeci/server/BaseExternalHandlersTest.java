package io.easeci.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ratpack.http.HttpMethod;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class BaseExternalHandlersTest {

    @BeforeEach
    void setup() {
        BaseExternalHandlers.clear();
    }

    @Test
    @DisplayName("Should correctly register endpoint")
    void registerSuccessTest() throws EndpointExistsException {
        EndpointDeclaration endpointDeclaration = EndpointDeclaration.builder()
                .httpMethod(HttpMethod.GET)
                .endpointUri("taken/endpoint/:firstParam:/")
                .build();
        BaseExternalHandlers.register(endpointDeclaration);

        assertEquals(1, BaseExternalHandlers.get().endpoints().size());
    }

    @Test
    @DisplayName("Should throw exception when trying to adding next same endpoint")
    void registerExceptionTest() {
        EndpointDeclaration endpointDeclaration = EndpointDeclaration.builder()
                .httpMethod(HttpMethod.GET)
                .endpointUri("taken/endpoint/:firstParam:/")
                .build();

        try {
            BaseExternalHandlers.register(endpointDeclaration);
        } catch (EndpointExistsException e) {
            e.printStackTrace();
        }

        assertAll(() -> assertThrows(EndpointExistsException.class, () -> BaseExternalHandlers.register(endpointDeclaration)),
                  () -> assertEquals(1, BaseExternalHandlers.get().endpoints().size()));
    }

    @Test
    @DisplayName("Should return false when trying to add endpoint that just exists in BaseExternalHandlers")
    void isPathAvailableTest() throws EndpointExistsException {
        EndpointDeclaration endpointDeclaration = EndpointDeclaration.builder()
                .httpMethod(HttpMethod.GET)
                .endpointUri("taken/endpoint/:firstParam:/")
                .build();
        BaseExternalHandlers.register(endpointDeclaration);

        boolean isPathAvailable = BaseExternalHandlers.isPathAvailable(endpointDeclaration.getHttpMethod(), endpointDeclaration.getEndpointUri());

        assertFalse(isPathAvailable);
    }

    @Test
    @DisplayName("Should return true when trying to register new endpoint that not exists yet")
    void isPathAvailableTrueTest() throws EndpointExistsException {
        EndpointDeclaration endpointDeclaration_1 = EndpointDeclaration.builder()
                .httpMethod(HttpMethod.GET)
                .endpointUri("taken/endpoint/:firstParam/")
                .build();

        EndpointDeclaration endpointDeclaration_2 = EndpointDeclaration.builder()
                .httpMethod(HttpMethod.GET)
                .endpointUri("taken/endpoint/:firstParam/:secondParam")
                .build();

        BaseExternalHandlers.register(endpointDeclaration_1);

        boolean isPathAvailable = BaseExternalHandlers.isPathAvailable(endpointDeclaration_2.getHttpMethod(), endpointDeclaration_2.getEndpointUri());

        assertTrue(isPathAvailable);
    }

    @Test
    @DisplayName("Should return false when trying to pass null as method's argument")
    void isPathAvailableNullTest() {
        boolean isPathAvailable = BaseExternalHandlers.isPathAvailable(null, null);

        assertFalse(isPathAvailable);
    }

    @Test
    @DisplayName("Should return true when trying to register same endpoint but with another amount of path variables")
    void isPathAvailableTrueNextText() {
        EndpointDeclaration endpointDeclaration = EndpointDeclaration.builder()
                .httpMethod(HttpMethod.GET)
                .endpointUri("taken/endpoint/:firstParam:/")
                .build();

        boolean isPathAvailable = BaseExternalHandlers.isPathAvailable(endpointDeclaration.getHttpMethod(), endpointDeclaration.getEndpointUri());

        assertTrue(isPathAvailable);
    }

    static List<String> provideUris() {
        return List.of(
                "api/v1/state/:",
                "api/v1/state/:someParam",
                "api/v1/state/:someParam/:nextParam",
                "api/v1/state"
        );
    }

    @Test
    @DisplayName("Should return all external registered endpoint's uris as a String")
    void listAllTest() {
        registerFakeEndpoints();

        List<String> allRegisteredUris = BaseExternalHandlers.listAll();

        assertEquals(3, allRegisteredUris.size());
    }

    @Test
    @DisplayName("Should return all external registered endpoint's EndpointDeclaration objects")
    void getTest() {
        registerFakeEndpoints();

        ExternalHandlers externalHandlers = BaseExternalHandlers.get();

        assertEquals(3, externalHandlers.endpoints().size());
    }

    private void registerFakeEndpoints() {
        List<EndpointDeclaration> endpointDeclarations = provideUris().subList(0, 3)
                .stream()
                .map(s -> EndpointDeclaration.builder()
                        .httpMethod(HttpMethod.GET)
                        .endpointUri(s)
                        .build())
                .collect(Collectors.toList());
        endpointDeclarations.forEach(endpointDeclaration -> {
            try {
                BaseExternalHandlers.register(endpointDeclaration);
            } catch (EndpointExistsException e) {
                e.printStackTrace();
            }
        });
    }

    @ParameterizedTest
    @MethodSource("provideUris")
    @DisplayName("Should return always the same base URI for provided argument")
    void extractBaseTest(String URI) {
        String base = BaseExternalHandlers.extractBase(URI);

        assertEquals(base, "api/v1/state");
    }

    @Test
    @DisplayName("Should return empty string when trying to pass null as argument")
    void extractBaseNullTest() {
        String base = BaseExternalHandlers.extractBase(null);

        assertTrue(base.isEmpty());
    }

    @Test
    @DisplayName("Should return correct value of parameters amount")
    void tokenizeTest() {
        final String URI_3 = "api/v1/state/:firstParam/:secondParam/:thirdParam";
        final String URI_2 = "api/v1/state/:firstParam/:secondParam:";
        final String URI_1 = "api/v1/state/:firstParam";

        int tokens_3 = BaseExternalHandlers.tokenize(URI_3);
        int tokens_2 = BaseExternalHandlers.tokenize(URI_2);
        int tokens_1 = BaseExternalHandlers.tokenize(URI_1);

        assertEquals(3, tokens_3);
        assertEquals(2, tokens_2);
        assertEquals(1, tokens_1);
    }

    @Test
    @DisplayName("Should return 0 (zero) when trying to pass null as argument")
    void tokenizeNullTest() {
        int tokens = BaseExternalHandlers.tokenize(null);

        assertEquals(0, tokens);
    }
}