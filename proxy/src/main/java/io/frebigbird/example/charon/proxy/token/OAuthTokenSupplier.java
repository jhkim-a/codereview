package io.frebigbird.example.charon.proxy.token;

import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;


@Slf4j
@Component
public class OAuthTokenSupplier {
    private final OAuthTokenSupplierProperties properties;

    private final AtomicBoolean isRefreshing = new AtomicBoolean(false);

    private String token;

    private RestTemplate restTemplate = new RestTemplate();

    public OAuthTokenSupplier(OAuthTokenSupplierProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
        this.restTemplate.getInterceptors()
            .add(new MyBasicAuthenticationInterceptor(properties.getUsername(), properties.getPassword()));
    }

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
        } catch (ObtainingTokenException e) {
            log.error("Obtaining access token error: {}", e.getMessage());
        } finally {
            synchronized (this) {
                this.notifyAll();
            }
        }
    }

    private String getAccessToken() {
        log.info("Access token refresh requested.");

        try {
            ResponseEntity<Token> response = restTemplate.exchange(
                properties.getUrl(),
                HttpMethod.POST,
                new HttpEntity<>(parameters(), headers()),
                Token.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ObtainingTokenException(response.getStatusCode().name());
            }

            return response.getBody().getAccessToken();
        } catch (HttpStatusCodeException e) {
            throw new ObtainingTokenException(e);
        } catch (ResourceAccessException e) {
            throw new ObtainingTokenException(e);
        }
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
