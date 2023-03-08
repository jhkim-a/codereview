package io.frebigbird.example.charon.proxy.http.client.interceptor;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class ServerInstanceListManager {
    private List<ServerInstance> activeInstances;

    private Set<ServerInstance> inactiveInstances;

    private int currentIndex;

    @Builder
    static class Config {
        @Builder.Default
        private long statusMonitoringInterval = 30_000;

        @Builder.Default
        private long healthCheckInterval = 30_000;

        @Builder.Default
        private String healthCheckProtocol = "http";

        @Builder.Default
        private String healthCheckPath = "/actuator/health";
    }

    public ServerInstanceListManager(List<String> instances) {
        this(Config.builder().build(), instances);
    }

    public ServerInstanceListManager(Config config, List<String> instances) {
        this.activeInstances = Collections.synchronizedList(instances.stream().map(ServerInstance::new).collect(Collectors.toList()));
        this.inactiveInstances = ConcurrentHashMap.newKeySet();
        this.currentIndex = 0;

        ExecutorService statusMonitoringExecutorService = Executors.newSingleThreadExecutor(r -> new Thread(r, "proxy-instance-status"));
        statusMonitoringExecutorService.execute(() -> {
            do {
                log.trace(">>> active/inactive/currentIndex: {}/{}/{}" + activeInstances.size(), inactiveInstances.size(), currentIndex);
                sleep(config.statusMonitoringInterval);
            } while (true);
        });

        InactiveInstanceHealthChecker inactiveInstanceHealthChecker = new InactiveInstanceHealthChecker(config.healthCheckProtocol, config.healthCheckPath);
        ExecutorService healthCheckExecutorService = Executors.newSingleThreadExecutor(r -> new Thread(r, "proxy-health-check"));
        healthCheckExecutorService.execute(() -> {
            do {
                inactiveInstanceHealthChecker.check();
                sleep(config.healthCheckInterval);
            } while (true);
        });
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized ServerInstance next() {
        if (currentIndex == Integer.MAX_VALUE) {
            currentIndex = 0;
        }
        return activeInstances.get(currentIndex++ % activeInstances.size());
    }

    public void failed(ServerInstance instanceInfo) {
        instanceToInActive(instanceInfo);
    }

    private void instanceToActive(ServerInstance instance) {
        activeInstances.add(instance);
        inactiveInstances.remove(instance);
    }

    private void instanceToInActive(ServerInstance instance) {
        inactiveInstances.add(instance);
        activeInstances.remove(instance);
    }

    @Getter
    @ToString
    @EqualsAndHashCode
    static class ServerInstance {
        private String instance;

        private String host;

        private int port;

        ServerInstance(String instance) {
            this.instance = instance;

            String[] splitValue = StringUtils.split(instance, ':');
            this.host = splitValue[0];
            this.port = Integer.parseInt(splitValue[1]);
        }
    }

    @AllArgsConstructor
    private class InactiveInstanceHealthChecker {
        private final RestTemplate restTemplate = new RestTemplate();

        private String protocol;

        private String path;

        private void check() {
            inactiveInstances.forEach((instance) -> {
                try {
                    ResponseEntity<String> response = restTemplate.getForEntity(
                        new StringBuilder().append(protocol).append("://").append(instance.getInstance()).append(path).toString(),
                        String.class);
                    instanceToActive(instance);
                } catch (HttpStatusCodeException e) {
                    instanceToActive(instance);
                } catch (ResourceAccessException e) {
                    log.trace("Cannot access resource (caused by {}): {}", e.getCause().getClass().getSimpleName(), instance.getInstance());
                }
            });
        }
    }
}
