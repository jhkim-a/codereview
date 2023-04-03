package io.frebigbird.example.charon.proxy.token;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("proxy.charon.oauth-token-supplier")
@Getter
public class OAuthTokenSupplierProperties {
    private String url = "http://localhost:9090/oauth2/token";

    private String username = "kakao-client";

    private String password = "kakao";

    private long waitTimeout = 10_000L;

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setWaitTimeout(long waitTimeout) {
        this.waitTimeout = waitTimeout;
    }
}
