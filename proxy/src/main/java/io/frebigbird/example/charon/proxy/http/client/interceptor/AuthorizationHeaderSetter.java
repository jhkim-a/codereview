package io.frebigbird.example.charon.proxy.http.client.interceptor;

import java.io.IOException;

import io.frebigbird.example.charon.proxy.token.OAuthTokenSupplier;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;


public class AuthorizationHeaderSetter implements ClientHttpRequestInterceptor {
    private final String AUTHORIZATION_HEADER = "Authorization";

    private final String TOKEN_TYPE = "Bearer ";

    private OAuthTokenSupplier tokenSupplier;

    public AuthorizationHeaderSetter(OAuthTokenSupplier tokenSupplier) {
        this.tokenSupplier = tokenSupplier;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders()
            .set(AUTHORIZATION_HEADER, TOKEN_TYPE + tokenSupplier.getToken());

        ClientHttpResponse response = execution.execute(request, body);

        if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            tokenSupplier.refresh();
        }
        return response;
    }
}
