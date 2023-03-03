package io.frebigbird.example.charon.proxy.backup;

import static com.github.mkopylec.charon.forwarding.interceptors.RequestForwardingInterceptorType.REQUEST_PROTOCOL_HEADERS_REWRITER;

import com.github.mkopylec.charon.forwarding.interceptors.HttpRequest;
import com.github.mkopylec.charon.forwarding.interceptors.HttpRequestExecution;
import com.github.mkopylec.charon.forwarding.interceptors.HttpResponse;
import com.github.mkopylec.charon.forwarding.interceptors.RequestForwardingInterceptor;
import com.github.mkopylec.charon.forwarding.interceptors.RequestForwardingInterceptorType;
import lombok.Setter;
import org.springframework.http.HttpHeaders;

@Setter
public class AuthorizationHeaderRewriter implements RequestForwardingInterceptor {
    private String propety;

    @Override
    public HttpResponse forward(HttpRequest request, HttpRequestExecution execution) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "ADMIN");

        request.setHeaders(headers);
        HttpResponse response = execution.execute(request);
        return response;
    }

    @Override
    public RequestForwardingInterceptorType getType() {
        return REQUEST_PROTOCOL_HEADERS_REWRITER;
    }
}
