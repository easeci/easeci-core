package io.easeci.api.easefile;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import io.easeci.commons.SerializeUtils;
import io.easeci.core.workspace.easefiles.DefaultEasefileManager;
import io.easeci.core.workspace.easefiles.EasefileManager;
import io.easeci.core.workspace.easefiles.EasefileOut;
import io.easeci.core.workspace.easefiles.EasefileStatus;
import io.easeci.server.EndpointDeclaration;
import io.easeci.server.InternalHandlers;
import ratpack.http.HttpMethod;

import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

import static java.util.Objects.nonNull;
import static ratpack.http.MediaType.APPLICATION_JSON;

public class EasefileManagementHandlers implements InternalHandlers {
    private final static String MAPPING = "easefile/";
    private final static String API_V2_MAPPING = "api/v2/" + MAPPING;
    private EasefileManager easefileManager;

    public EasefileManagementHandlers() {
        this.easefileManager = DefaultEasefileManager.getInstance();
    }

    @Override
    public List<EndpointDeclaration> endpoints() {
        return List.of(
                getRootEasefileDirectory(),
                scanWorkspaceDirectoryTree(),
                scanPathDirectoryTree(),
                createDirectory(),
                deleteDirectory(),
                getEasefileContent(),
                addEasefileFullPath(),
                addEasefileByName(),
                updateEasefile(),
                deleteEasefile()
        );
    }

    public EndpointDeclaration getRootEasefileDirectory() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.GET)
                .endpointUri(MAPPING + "workspace/root")
                .handler(ctx -> ctx.getRequest().getBody()
                        .map(typedData -> easefileManager.getRootEasefilePath())
                        .map(EasefileWorkspaceResponse::new)
                        .map(SerializeUtils::write)
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    public EndpointDeclaration scanWorkspaceDirectoryTree() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.GET)
                .endpointUri(MAPPING + "workspace/scan")
                .handler(ctx -> ctx.getRequest().getBody()
                        .map(typedData -> easefileManager.scan())
                        .map(EasefileWorkspaceResponse::new)
                        .map(SerializeUtils::write)
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    public EndpointDeclaration scanPathDirectoryTree() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.POST)
                .endpointUri(MAPPING + "workspace/scan/path")
                .handler(ctx -> ctx.getRequest().getBody()
                        .map(typedData -> {
                            EasefileScanPathRequest easefileScanPathRequest = SerializeUtils.read(typedData.getBytes(), EasefileScanPathRequest.class).orElseThrow();
                            return easefileManager.scan(Paths.get(easefileScanPathRequest.getPath()));
                        })
                        .map(EasefileWorkspaceResponse::new)
                        .mapError(this::errorMapping)
                        .map(SerializeUtils::write)
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    public EndpointDeclaration createDirectory() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.POST)
                .endpointUri(MAPPING + "workspace/directory/create")
                .handler(ctx -> ctx.getRequest().getBody()
                        .map(typedData -> {
                            DirectoryRequest directoryRequest = SerializeUtils.read(typedData.getBytes(), DirectoryRequest.class).orElseThrow();
                            return easefileManager.createDirectory(Paths.get(directoryRequest.getPath()));
                        }).map(tuple -> DirectoryResponse.of(tuple._1, tuple._2, null, tuple._3))
                        .mapError(this::directoryErrorMapping)
                        .map(SerializeUtils::write)
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    public EndpointDeclaration deleteDirectory() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.POST)
                .endpointUri(MAPPING + "workspace/directory/delete")
                .handler(ctx -> ctx.getRequest().getBody()
                        .map(typedData -> {
                            DirectoryRequest directoryRequest = SerializeUtils.read(typedData.getBytes(), DirectoryRequest.class).orElseThrow();
                            return easefileManager.deleteDirectory(Paths.get(directoryRequest.getPath()), directoryRequest.isForce());
                        }).map(tuple -> DirectoryResponse.of(tuple._1, tuple._2))
                        .mapError(this::directoryErrorMapping)
                        .map(SerializeUtils::write)
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    public EndpointDeclaration getEasefileContent() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.POST)
                .endpointUri(MAPPING + "load")
                .handler(ctx -> ctx.getRequest().getBody()
                        .map(typedData -> {
                            GetEasefileRequest easefileRequest = SerializeUtils.read(typedData.getBytes(), GetEasefileRequest.class).orElseThrow();
                            return easefileManager.load(Paths.get(easefileRequest.getPath()));
                        }).map(this::mapGetResponse)
                        .mapError(this::easefileErrorMapping)
                        .map(SerializeUtils::write)
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    private EasefileResponse mapGetResponse(EasefileOut easefileOut) {
        if (nonNull(easefileOut.getErrorMessage())) {
            return EasefileResponse.withError(easefileOut.getErrorMessage(), easefileOut.getEasefileStatus());
        }
        byte[] encodedContent = Base64.getEncoder().encode(easefileOut.getEasefileContent().getBytes());
        String encodedContentAsString = new String(encodedContent, Charset.defaultCharset());
        return EasefileResponse.of(easefileOut.getEasefileStatus(), easefileOut.getErrorMessage(), encodedContentAsString);
    }

    public EndpointDeclaration addEasefileFullPath() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.POST)
                .endpointUri(MAPPING + "save")
                .handler(ctx -> ctx.getRequest().getBody()
                        .map(typedData -> {
                            AddEasefileRequest addEasefileRequest = SerializeUtils.read(typedData.getBytes(), AddEasefileRequest.class).orElseThrow();
                            String decodedEasefileContent = decode(addEasefileRequest.getEncodedEasefileContent());
                            return easefileManager.save(Paths.get(addEasefileRequest.getPath()), decodedEasefileContent);
                        }).map(this::mapSaveResponse)
                        .mapError(this::addEasefileErrorMapping)
                        .map(SerializeUtils::write)
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    public EndpointDeclaration addEasefileByName() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.POST)
                .endpointUri(API_V2_MAPPING + "save")
                .handler(ctx -> ctx.getRequest().getBody()
                        .map(typedData -> {
                            AddEasefileRequestV2 addEasefileRequest = SerializeUtils.read(typedData.getBytes(), AddEasefileRequestV2.class).orElseThrow();
                            String decodedEasefileContent = decode(addEasefileRequest.getEncodedEasefileContent());
                            return easefileManager.save(addEasefileRequest.getFilename(), decodedEasefileContent);
                        }).map(this::mapSaveResponse)
                        .mapError(this::addEasefileErrorMapping)
                        .map(SerializeUtils::write)
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    private String decode(String encodedData) {
        byte[] decoded = Base64.getDecoder().decode(encodedData);
        return new String(decoded, Charset.defaultCharset());
    }

    private AddEasefileResponse mapSaveResponse(EasefileOut easefileOut) {
        if (nonNull(easefileOut.getErrorMessage())) {
            return AddEasefileResponse.withError(easefileOut.getErrorMessage(), easefileOut.getEasefileStatus());
        }
        return AddEasefileResponse.of(easefileOut.getFilePath(), easefileOut.getEasefileStatus());
    }

    public EndpointDeclaration updateEasefile() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.POST)
                .endpointUri(MAPPING + "edit")
                .handler(ctx -> ctx.getRequest().getBody()
                        .map(typedData -> {
                            AddEasefileRequest addEasefileRequest = SerializeUtils.read(typedData.getBytes(), AddEasefileRequest.class).orElseThrow();
                            String decodedEasefileContent = decode(addEasefileRequest.getEncodedEasefileContent());
                            return easefileManager.update(Paths.get(addEasefileRequest.getPath()), decodedEasefileContent);
                        }).map(this::mapSaveResponse)
                        .mapError(this::addEasefileErrorMapping)
                        .map(SerializeUtils::write)
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    public EndpointDeclaration deleteEasefile() {
        return EndpointDeclaration.builder()
                .httpMethod(HttpMethod.POST)
                .endpointUri(MAPPING + "delete")
                .handler(ctx -> ctx.getRequest().getBody()
                        .map(typedData -> {
                            AddEasefileRequest addEasefileRequest = SerializeUtils.read(typedData.getBytes(), AddEasefileRequest.class).orElseThrow();
                            return easefileManager.delete(Paths.get(addEasefileRequest.getPath()));
                        }).map(this::mapDeleteResponse)
                        .mapError(this::addEasefileErrorMapping)
                        .map(SerializeUtils::write)
                        .then(bytes -> ctx.getResponse().contentType(APPLICATION_JSON).send(bytes)))
                .build();
    }

    private AddEasefileResponse mapDeleteResponse(Boolean deleteResult) {
        return AddEasefileResponse.of(deleteResult ? EasefileStatus.REMOVED_CORRECTLY : EasefileStatus.REMOVE_FAILED);
    }

    private EasefileWorkspaceResponse errorMapping(Throwable throwable) {
        if (throwable instanceof UnrecognizedPropertyException) {
            return EasefileWorkspaceResponse.withError("Data in request body is not correct.");
        }
        return EasefileWorkspaceResponse.withError("Not expected, unrecognized exception occurred while processing request");
    }

    private DirectoryResponse directoryErrorMapping(Throwable throwable) {
        if (throwable instanceof UnrecognizedPropertyException) {
            return DirectoryResponse.withError("Data in request body is not correct.");
        }
        return DirectoryResponse.withError("Not expected, unrecognized exception occurred while processing request");
    }

    private EasefileResponse easefileErrorMapping(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            return EasefileResponse.withError("File not exists or you has no access rights", EasefileStatus.NOT_EXISTS);
        }
        if (throwable instanceof UnrecognizedPropertyException) {
            return EasefileResponse.withError("Data in request body is not correct.", EasefileStatus.REQUEST_ERROR);
        }
        return EasefileResponse.withError("Not expected, unrecognized exception occurred while processing request", EasefileStatus.REQUEST_ERROR);
    }

    private AddEasefileResponse addEasefileErrorMapping(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            return AddEasefileResponse.withError("File not exists or you has no access rights", EasefileStatus.NOT_EXISTS);
        }
        if (throwable instanceof UnrecognizedPropertyException) {
            return AddEasefileResponse.withError("Data in request body is not correct.", EasefileStatus.REQUEST_ERROR);
        }
        return AddEasefileResponse.withError("Not expected, unrecognized exception occurred while processing request", EasefileStatus.REQUEST_ERROR);
    }
}
