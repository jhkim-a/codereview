package io.frebigbird.example.charon.proxy.http.client.interceptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ServerInstanceListTest {
    @Test
    public void next() throws InterruptedException {
        ServerInstanceListManager serverInstanceList = new ServerInstanceListManager(Arrays.asList("localhost:7070", "localhost:7071", "localhost:7072"));
        ExecutorService service = Executors.newFixedThreadPool(100);
        final ArrayList<Throwable> ex = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            service.execute(() -> {
                for (int j = 0; j < 1000; j++) {
                    try {
                        System.out.println(serverInstanceList.next());
                    } catch (ArrayIndexOutOfBoundsException e) {
                        ex.add(e);
                    }
                }
            });
        }
        service.shutdown();
        service.awaitTermination(1, TimeUnit.MINUTES);
        if (ex.size() > 0) {
            Assertions.assertTrue(false);
        } else  {
            Assertions.assertTrue(true);
        }
    }
}