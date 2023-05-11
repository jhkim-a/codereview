package io.frebigbird.example.charon.proxy.token;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;

@Slf4j
public class MyBasicAuthenticationInterceptor extends BasicAuthenticationInterceptor {
    public MyBasicAuthenticationInterceptor(String username, String password) {
        super(username, password);
    }

    @Override
    public ClientHttpResponse intercept(
        HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        return super.intercept(request, body, execution);
    }
}
