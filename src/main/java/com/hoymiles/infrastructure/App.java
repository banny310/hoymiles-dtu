package com.hoymiles.infrastructure;

import com.hoymiles.application.AppController;
import com.hoymiles.infrastructure.dtu.DtuClient;
import com.hoymiles.infrastructure.dtu.utils.RxUtils;
import com.typesafe.config.Config;
import io.netty.channel.ConnectTimeoutException;
import io.reactivex.rxjava3.core.Observable;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Dependent
@Log4j2
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class App {
    private final ScheduledExecutorService executor;
    private final Config config;
    private final DtuClient dtuClient;
    private final IMqttClient mqttClient;
    private final AppController appController;

    public void run() throws InterruptedException, MqttException {
        initDtuConnection();
        initMqttConnection();

        appController.start();

        dtuClient.close();
        mqttClient.close();
    }

    public void halt() throws InterruptedException {
        executor.shutdownNow();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }

    private void initDtuConnection() {
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

    private void initMqttConnection() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(5);
        options.setWill("hoymiles-dtu/bridge/state", "offline" .getBytes(), 1, true);

        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                log.error("connectionLost: " + cause.getMessage(), cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

        String mqttUri = String.format("tcp://%s:%d", config.getString("mqtt.host"), config.getInt("mqtt.port"));
        log.info("Connecting to MQTT: " + mqttUri);

        Observable.create(emitter -> {
                    mqttClient.connect(options);
                    emitter.onNext(1);
                })
                .retry(3, e -> {
                    log.warn("Mqtt connection error: " + e.getMessage());
                    log.warn("Retrying...");
                    return true;
                }).blockingFirst();
        log.info("MQTT connection success");
    }
}




