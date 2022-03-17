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
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Dependent
@Log4j2
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class App {
    private final Config config;
    private final BlockingQueue<Runnable> queue;
    private final DtuClient dtuClient;
    private final IMqttClient mqttClient;
    private final AppController appController;
    private boolean run = false;

    public void run() throws InterruptedException, MqttException {
        run = true;
        initDtuConnection();
        initMqttConnection();

        queue.put(appController::start);

        while (true) {
            if (!run) {
                dtuClient.close();
                mqttClient.close();
                return;
            }

            try {
                queue.take().run();
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public void halt() {
        run = false;
    }

    public boolean isRunning() {
        return run;
    }

    private void initDtuConnection() {
        // initialize connection to DTU
        String dtuHost = config.getString("dtu.host");
        int dtuPort = config.getInt("dtu.port");
        assert dtuHost != null;
        assert dtuPort != 0;

        log.info("Connecting to DTU: " + dtuHost + ":" + dtuPort);
        Observable.create(emitter -> dtuClient.connect(dtuHost, dtuPort))
                .retryWhen(RxUtils.retryPredicate(
                        e -> e instanceof ConnectTimeoutException, 3,
                        count -> (long) Math.pow(2, count - 1), TimeUnit.SECONDS))
                .subscribe(
                        (v) -> log.info("DTU connection success"),
                        e -> log.error(e.getMessage(), e)
                );
    }

    private void initMqttConnection() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        options.setWill("hoymiles-solar/bridge/state", "offline".getBytes(), 1, true);

        String mqttUri = String.format("tcp://%s:%d", config.getString("mqtt.host"), config.getInt("mqtt.port"));
        log.info("Connecting to MQTT: " + mqttUri);
        Observable.create(emitter -> mqttClient.connect(options))
                .retry(3, e -> {
                    log.warn("Mqtt connection error: " + e.getMessage());
                    log.warn("Retrying...");
                    return true;
                })
                .subscribe(
                        (v) -> log.info("MQTT connection success"),
                        e -> log.error(e.getMessage(), e)
                );
    }
}




