package io.frebigbird.example.charon.proxy.backup;

import com.github.mkopylec.charon.forwarding.interceptors.RequestForwardingInterceptorConfigurer;

public class AuthorizationHeaderRewriterConfigurer extends RequestForwardingInterceptorConfigurer<AuthorizationHeaderRewriter> {
    protected AuthorizationHeaderRewriterConfigurer() {
        super(new AuthorizationHeaderRewriter());
    }

    static AuthorizationHeaderRewriterConfigurer customInterceptor() {
        return new AuthorizationHeaderRewriterConfigurer();
    }

    AuthorizationHeaderRewriterConfigurer property(String property) {
        configuredObject.setPropety(property);
        return this;
    }
}
