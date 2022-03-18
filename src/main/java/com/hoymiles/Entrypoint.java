package com.hoymiles;

import com.hoymiles.infrastructure.App;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.enterprise.util.TypeLiteral;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.*;

@Log4j2
public class Entrypoint {
    public static void main(String[] args) throws InterruptedException {
        log.info("Starting...");

        final SeContainerInitializer initializer = SeContainerInitializer.newInstance();
        try (final SeContainer container = initializer.initialize()) {
            assert container != null;
            App app = container.select(App.class).get();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @SneakyThrows(InterruptedException.class)
                public void run() {
                    log.info("Shutdown called - stopping...");
                    app.halt();
                }
            });

            try {
                app.run();
            } catch (Exception e) {
                app.halt();
                log.error("Uncaught exception: " + e.getMessage(), e);
            }

            log.info("Shutdown complete");
        }
    }
}
