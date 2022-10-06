package com.hoymiles;

import com.hoymiles.infrastructure.App;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Log4j2
public class Entrypoint {
    public static void main(String[] args) throws InterruptedException {
        log.info("Starting...");

        final SeContainerInitializer initializer = SeContainerInitializer.newInstance();
        final SeContainer container = initializer.initialize();

        assert container != null;
        App app = container.select(App.class).get();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown called - stopping...");
            app.halt();
        }));

        ExecutorService executor = container.select(ExecutorService.class).get();
        executor.submit(() -> {
            try {
                app.run();
            } catch (Exception e) {
                log.error("Uncaught exception: " + e.getMessage(), e);
            }
        });

        while (!executor.awaitTermination(1, TimeUnit.SECONDS)) {}

//        log.info("Starting server");
//        TcpServer server = container.select(TcpServer.class).get();
//        server.start(10081);

        log.info("Shutdown complete");

    }
}
