package io.frebigbird.example.charon.proxy;

import static com.github.mkopylec.charon.configuration.CharonConfigurer.charonConfiguration;
import static com.github.mkopylec.charon.configuration.RequestMappingConfigurer.requestMapping;
import static com.github.mkopylec.charon.forwarding.OkClientHttpRequestFactoryCreatorConfigurer.okClientHttpRequestFactoryCreator;
import static com.github.mkopylec.charon.forwarding.RestTemplateConfigurer.restTemplate;
import static com.github.mkopylec.charon.forwarding.TimeoutConfigurer.timeout;
import static com.github.mkopylec.charon.forwarding.interceptors.resilience.RetryerConfigurer.retryer;
import static io.github.resilience4j.retry.RetryConfig.from;
import static io.github.resilience4j.retry.RetryConfig.ofDefaults;
import static java.time.Duration.ZERO;
import static java.time.Duration.ofMillis;
import static java.util.Arrays.asList;

import com.github.mkopylec.charon.configuration.CharonConfigurer;
import com.github.mkopylec.charon.forwarding.interceptors.HttpResponse;
import io.frebigbird.example.charon.proxy.http.client.interceptor.AuthorizationHeaderSetter;
import io.frebigbird.example.charon.proxy.http.client.interceptor.OutgoingServerSetter;
import io.frebigbird.example.charon.proxy.token.OAuthTokenSupplier;
import io.github.resilience4j.retry.RetryConfig;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class CharonConfiguration {
    private final OAuthTokenSupplier tokenSupplier;

    @Bean
    CharonConfigurer charonConfigurer() {
        return charonConfiguration()
            .add(requestMapping("in-going")
                .pathRegex("/hello*")
                .set(restTemplate()
                    .set(asList(new AuthorizationHeaderSetter(tokenSupplier), new OutgoingServerSetter("localhost:7071", "localhost:7070")))
                    .set(timeout().connection(ofMillis(100)).read(ofMillis(1000)).write(ofMillis(500)))
                    .set(okClientHttpRequestFactoryCreator().httpClient(
                        new OkHttpClient.Builder()
                            .followRedirects(false)
                            .followSslRedirects(false)
                    ))
                )
                .set(retryer().configuration(defaultRetryConfiguration()))
            )
            .add(requestMapping("out-going")
                .pathRegex("/world*")
            );
    }

    private RetryConfig.Builder<HttpResponse> defaultRetryConfiguration() {
        return from(ofDefaults());
    }

    private RetryConfig.Builder<HttpResponse> customRetryConfiguration() {
        return RetryConfig.<HttpResponse>custom()
            .waitDuration(ZERO)
            .retryOnResult(response -> response.getStatusCode().is5xxServerError())
            .retryExceptions(Throwable.class);
    }
}
