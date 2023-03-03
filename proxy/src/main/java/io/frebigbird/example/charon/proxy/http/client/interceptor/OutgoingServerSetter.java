package io.frebigbird.example.charon.proxy.http.client.interceptor;

import java.io.IOException;

import com.github.mkopylec.charon.forwarding.interceptors.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.util.UriComponentsBuilder;

public class OutgoingServerSetter implements ClientHttpRequestInterceptor {
    private ServerInstanceList serverInstance;

    public OutgoingServerSetter(String... outgoingServers) {
        this.serverInstance = new ServerInstanceList(outgoingServers);
    }

    @Override
    public ClientHttpResponse intercept(org.springframework.http.HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        HttpRequest charonHttpRequest = (HttpRequest) request;
        ServerInstanceList.ServerInstance connectionInfo = serverInstance.next();
        charonHttpRequest.setUri(
            UriComponentsBuilder
                .fromUri(request.getURI())
                .host(connectionInfo.getHost())
                .port(connectionInfo.getPort())
                .build(true)
                .toUri()
        );

        return execution.execute(request, body);
    }
}
