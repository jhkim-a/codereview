package io.frebigbird.example.charon.proxy;

import static com.github.mkopylec.charon.configuration.CharonConfigurer.charonConfiguration;
import static com.github.mkopylec.charon.configuration.RequestMappingConfigurer.requestMapping;
import static com.github.mkopylec.charon.forwarding.OkClientHttpRequestFactoryCreatorConfigurer.okClientHttpRequestFactoryCreator;
import static com.github.mkopylec.charon.forwarding.RestTemplateConfigurer.restTemplate;
import static com.github.mkopylec.charon.forwarding.TimeoutConfigurer.timeout;
import static com.github.mkopylec.charon.forwarding.interceptors.resilience.RetryerConfigurer.retryer;
import static com.github.mkopylec.charon.forwarding.interceptors.rewrite.RegexRequestPathRewriterConfigurer.regexRequestPathRewriter;
import static io.github.resilience4j.retry.RetryConfig.from;
import static io.github.resilience4j.retry.RetryConfig.ofDefaults;
import static java.time.Duration.ofMillis;
import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.github.mkopylec.charon.configuration.CharonConfigurer;
import com.github.mkopylec.charon.forwarding.interceptors.HttpResponse;
import io.frebigbird.example.charon.proxy.http.client.interceptor.AuthorizationHeaderSetter;
import io.frebigbird.example.charon.proxy.http.client.interceptor.OutgoingServerSetter;
import io.frebigbird.example.charon.proxy.token.OAuthTokenSupplier;
import io.github.resilience4j.retry.RetryConfig;
import lombok.RequiredArgsConstructor;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CharonConfiguration {
    private final OAuthTokenSupplier tokenSupplier;

    @Bean
    CharonConfigurer charonConfigurer() {
        return charonConfiguration()
            .add(requestMapping("hello-mapping")
                .pathRegex("/helloworld/hello.*")
                .set(regexRequestPathRewriter().paths("/helloworld/(?<path>.*)", "/<path>"))
                .set(restTemplate()
                    .set(asList(
                        new AuthorizationHeaderSetter(tokenSupplier),
                        new OutgoingServerSetter("http", Arrays.asList("localhost:7072", "localhost:7071", "localhost:7070"))))
                    .set(timeout()
                        .connection(ofMillis(10_000))
                        .read(ofMillis(10_000))
                        .write(ofMillis(10_000)))
                    .set(okClientHttpRequestFactoryCreator().httpClient(
                        new OkHttpClient.Builder()
                            .connectionPool(new ConnectionPool(5, 1, TimeUnit.MINUTES))
                            .followRedirects(false)
                            .followSslRedirects(false)
                    ))
                )
                .set(retryer().configuration(retryConfiguration()))
            )
            .add(requestMapping("world-mapping")
                .pathRegex("/helloworld/world.*")
                .set(regexRequestPathRewriter().paths("/helloworld/(?<path>.*)", "/<path>"))
                .set(restTemplate()
                    .set(asList(
                        new AuthorizationHeaderSetter(tokenSupplier),
                        new OutgoingServerSetter("http", Arrays.asList("localhost:7072", "localhost:7071", "localhost:7070"))))
                    .set(timeout()
                        .connection(ofMillis(10_000))
                        .read(ofMillis(10_000))
                        .write(ofMillis(10_000)))
                    .set(okClientHttpRequestFactoryCreator().httpClient(
                        new OkHttpClient.Builder()
                            .connectionPool(new ConnectionPool(5, 1, TimeUnit.MINUTES))
                            .followRedirects(false)
                            .followSslRedirects(false)
                    ))
                )
                .set(retryer().configuration(retryConfiguration()))
            )
             .add(requestMapping("bird-mapping")
                .pathRegex("/helloworld/world.*")
                .set(regexRequestPathRewriter().paths("/helloworld/(?<path>.*)", "/<path>"))
                .set(restTemplate()
                    .set(asList(
                        new AuthorizationHeaderSetter(tokenSupplier),
                        new OutgoingServerSetter("http", Arrays.asList("localhost:7072", "localhost:7071", "localhost:7070"))))
                    .set(timeout()
                        .connection(ofMillis(10_000))
                        .read(ofMillis(10_000))
                        .write(ofMillis(10_000)))
                    .set(okClientHttpRequestFactoryCreator().httpClient(
                        new OkHttpClient.Builder()
                            .connectionPool(new ConnectionPool(5, 1, TimeUnit.MINUTES))
                            .followRedirects(false)
                            .followSslRedirects(false)
                    ))
                )
                .set(retryer().configuration(retryConfiguration()))
            );
    }

    /**
     * return RetryConfig.<HttpResponse>custom()
     *     .maxAttempts(10);
     *
     * return RetryConfig.<HttpResponse>custom()
     *     .waitDuration(ZERO)
     *     .retryOnResult(response -> response.getStatusCode().is5xxServerError())
     *     .retryExceptions(Throwable.class);
     */
    private RetryConfig.Builder<HttpResponse> retryConfiguration() {
        return from(ofDefaults());
    }
}

