package com.hoymiles.infrastructure;

import com.hoymiles.application.AppController;
import com.hoymiles.infrastructure.dtu.DtuConnectionLostEvent;
import com.hoymiles.infrastructure.dtu.DtuClient;
import com.hoymiles.infrastructure.dtu.utils.RxUtils;
import com.hoymiles.infrastructure.mqtt.MqttConnectedEvent;
import com.hoymiles.infrastructure.mqtt.MqttConnectionConfigProvider;
import com.hoymiles.infrastructure.mqtt.MqttConnectionLostEvent;
import com.typesafe.config.Config;
import io.netty.channel.ConnectTimeoutException;
import io.reactivex.rxjava3.core.Observable;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.*;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Dependent
@Log4j2
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class App {
    private final BeanManager beanManager;
    private final ScheduledExecutorService executor;
    private final Config config;
    private final DtuClient dtuClient;
    private final IMqttClient mqttClient;
    private final AppController appController;
    private final MqttConnectionConfigProvider connectionConfigProvider;
    private final AtomicBoolean alive = new AtomicBoolean(true);

    public Void handleDtuConnectionLost(@Observes @Priority(1) @NotNull DtuConnectionLostEvent event) {
        log.error("Dtu connection lost: {}", event.getCause().getMessage(), event.getCause());
        initDtuConnection();
        return null;
    }

    public Void handleMqttConnectionLost(@Observes @Priority(1) @NotNull MqttConnectionLostEvent event) {
        log.error("Mqtt connection lost: {}", event.getCause().getMessage(), event.getCause());
        initMqttConnection();
        return null;
    }

    public void run() throws InterruptedException, MqttException {
        initDtuConnection();
        initMqttConnection();

        appController.start();

        if (dtuClient.isConnected())
            dtuClient.disconnect();

        if (mqttClient.isConnected())
            mqttClient.disconnect();

        mqttClient.close();
    }

    public void halt() throws InterruptedException {
        log.info("Stopping...");
        alive.set(false);
        executor.shutdownNow();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }

    private void initDtuConnection() {
        if (alive.get()) {
            // initialize connection to DTU
            String dtuHost = config.getString("dtu.host");
            int dtuPort = config.getInt("dtu.port");
            assert dtuHost != null;
            assert dtuPort != 0;

            log.info("Connecting to DTU: " + dtuHost + ":" + dtuPort);
            Observable.create(emitter -> {
                        dtuClient.connect(dtuHost, dtuPort);
                        emitter.onNext(1);
                    })
                    .retryWhen(RxUtils.retryPredicate(
                            e -> e instanceof ConnectTimeoutException, 3,
                            count -> (long) Math.pow(2, count - 1), TimeUnit.SECONDS))
                    .blockingFirst();
            log.info("DTU connection success");
        }
    }

    private void initMqttConnection() {
        if (alive.get()) {
            log.info("Connecting to MQTT: {}", connectionConfigProvider.getConnectionUri());

            Observable.create(emitter -> {
                        mqttClient.connect(connectionConfigProvider.getConnectionOptions());
                        emitter.onNext(1);
                    })
                    .retry(3, e -> {
                        log.warn("Mqtt connection error: " + e.getMessage());
                        log.warn("Retrying...");
                        return true;
                    }).blockingFirst();

            log.info("MQTT connection success");
            beanManager.fireEvent(new MqttConnectedEvent(connectionConfigProvider.getConnectionUri()));
        }
    }
}




