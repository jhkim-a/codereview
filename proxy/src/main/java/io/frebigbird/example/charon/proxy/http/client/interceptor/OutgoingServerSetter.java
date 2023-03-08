package io.frebigbird.example.charon.proxy.http.client.interceptor;

import java.io.IOException;
import java.util.List;

import com.github.mkopylec.charon.forwarding.interceptors.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.util.UriComponentsBuilder;

public class OutgoingServerSetter implements ClientHttpRequestInterceptor {
    private String protocol;

    private ServerInstanceListManager serverInstanceListManager;

    public OutgoingServerSetter(String protocol, List<String> outgoingServers) {
        this.protocol = protocol;
        this.serverInstanceListManager = new ServerInstanceListManager(
            ServerInstanceListManager.Config.builder()
                .statusMonitoringInterval(10_000)
                .healthCheckInterval(30_000)
                .healthCheckProtocol(protocol)
                .healthCheckPath("/actuator/health")
                .build(),
            outgoingServers);
    }

    @Override
    public ClientHttpResponse intercept(org.springframework.http.HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        HttpRequest charonHttpRequest = (HttpRequest) request;
        ServerInstanceListManager.ServerInstance instanceInfo = serverInstanceListManager.next();
        charonHttpRequest.setUri(
            UriComponentsBuilder
                .fromUri(request.getURI())
                .scheme(protocol)
                .host(instanceInfo.getHost())
                .port(instanceInfo.getPort())
                .build(true)
                .toUri()
        );

        try {
            return execution.execute(request, body);
        } catch (IOException e) {
            serverInstanceListManager.failed(instanceInfo);
            throw e;
        }
    }
}
