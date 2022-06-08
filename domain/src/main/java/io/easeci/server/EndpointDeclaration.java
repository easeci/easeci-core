package io.easeci.server;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ratpack.handling.Handler;
import ratpack.http.HttpMethod;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndpointDeclaration {
    private HttpMethod httpMethod;
    private String endpointUri;
    private Handler handler;
    private boolean multiEndpointDeclaration;

    @Override
    public String toString() {
        return "EndpointDeclaration{" +
                "httpMethod=" + httpMethod +
                ", endpointUri='" + endpointUri + '\'' +
                ", handler=" + handler +
                '}';
    }
}
