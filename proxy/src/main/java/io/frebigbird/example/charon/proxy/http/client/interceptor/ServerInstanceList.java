package io.frebigbird.example.charon.proxy.http.client.interceptor;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

public class ServerInstanceList {
    private ServerInstance[] serverInstances;

    private AtomicInteger nextIndex;

    public ServerInstanceList(String... instances) {
        this.serverInstances = Arrays.stream(instances).map(ServerInstance::new).toArray(ServerInstance[]::new);
        this.nextIndex = new AtomicInteger(0);
    }

    public ServerInstance next() {
        int index = nextIndex.getAndIncrement();
        nextIndex.compareAndSet(serverInstances.length, 0);

        return serverInstances[index];
    }

    @Getter
    static class ServerInstance {
        private String host;

        private int port;

        ServerInstance(String instance) {
            String[] splitted = StringUtils.split(instance, ':');
            this.host = splitted[0];
            this.port = Integer.parseInt(splitted[1]);
        }
    }
}
