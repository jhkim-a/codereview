package io.frebigbird.example.charon.proxy.token;

import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthTokenSupplier {
    private final OAuthTokenSupplierProperties properties;

    private final AtomicBoolean isRefreshing = new AtomicBoolean(false);

    private String token;

    public String getToken() {
        if (StringUtils.isEmpty(token)) {
            refresh();
        }
        return token;
    }

    public void refresh() {
        if (isRefreshing.get()) {
            waitInRefresh();
        } else {
            isRefreshing.set(true);
            getAccessTokenInRefresh();
            isRefreshing.set(false);
        }
    }

    private synchronized void waitInRefresh() {
        try {
            this.wait(properties.getWaitTimeout());
        } catch (InterruptedException e) {
            log.warn("Thread interrupted");
        }
    }

    private void getAccessTokenInRefresh() {
        try {
            this.token = getAccessToken();
        } finally {
            synchronized (this) {
                this.notifyAll();
            }
        }
    }

    private String getAccessToken() {
        log.info("token refresh requested.");

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors()
            .add(new BasicAuthenticationInterceptor(properties.getUsername(), properties.getPassword()));

        ResponseEntity<Token> response = restTemplate.exchange(
            properties.getUrl(),
            HttpMethod.POST,
            new HttpEntity<>(parameters(), headers()),
            Token.class
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("Obtaining access token error (status:{})", response.getStatusCodeValue());
        }

        return (String) response.getBody().getAccessToken();
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    private MultiValueMap<String, String> parameters() {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.set("grant_type", "client_credentials");
        return parameters;
    }

    @Getter
    private static class Token {
        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("token_type")
        private String tokenType;

        @JsonProperty("expire_in")
        private int expireIn;
    }
}
