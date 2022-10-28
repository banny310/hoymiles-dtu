package com.hoymiles.infrastructure;

import com.hoymiles.application.AppController;
import com.hoymiles.infrastructure.dtu.DtuClient;
import com.hoymiles.infrastructure.dtu.DtuClientListenerExecutorWrapper;
import com.hoymiles.infrastructure.dtu.DtuMessage;
import com.hoymiles.infrastructure.dtu.DtuMessageRouter;
import com.hoymiles.infrastructure.dtu.utils.RxUtils;
import com.hoymiles.infrastructure.mqtt.MqttConnectedEvent;
import com.hoymiles.infrastructure.mqtt.MqttConnectionConfigProvider;
import com.typesafe.config.Config;
import io.reactivex.rxjava3.core.Observable;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Dependent
@Log4j2
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class App {
    private final BeanManager beanManager;
    private final ExecutorService executor;
    private final Config config;
    private final DtuClient dtuClient;
    private final DtuMessageRouter messageRouter;
    private final IMqttClient mqttClient;
    private final AppController appController;
    private final MqttConnectionConfigProvider connectionConfigProvider;

    DtuClient.Listener dtuListener = new DtuClient.Listener() {
        @Override
        public void onEvent(DtuMessage dtuMessage) {
            messageRouter.handle(dtuMessage);
        }

        @Override
        public void onError(Throwable throwable) {
            log.error("Exception: {}", throwable.getMessage(), throwable);
        }

        @Override
        public void onConnectionLost(Throwable cause) {
            String reason = Optional.ofNullable(cause).map(Throwable::getMessage).orElse("(null)");
            log.error("DTU connection lost: {}", reason, cause);
            initDtuConnection();
        }
    };

    MqttCallback mqttCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            String reason = Optional.ofNullable(cause).map(Throwable::getMessage).orElse("(null)");
            log.error("Mqtt connection lost: {}", reason, cause);
            initMqttConnection();
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            log.info("Mqtt message: topic={}, message={}", topic, message.toString());
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
        }
    };

    public void run() {
        log.info("Application Hoymiles DTU starting...");

        initDtuConnection();
        initMqttConnection();

        appController.start();

        log.info("Bootstrap finished. Waiting for incoming messages...");
    }

    @SneakyThrows({InterruptedException.class})
    public void halt() {
        log.info("Stopping...");

        appController.stop();

        executor.shutdownNow();
        //noinspection ResultOfMethodCallIgnored
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    private void initDtuConnection() {
        // initialize connection to DTU
        String dtuHost = config.getString("dtu.host");
        int dtuPort = config.getInt("dtu.port");
        int watchdogTimeout = config.getInt("dtu.watchdog_timeout");
        assert dtuHost != null;
        assert dtuPort != 0;
        assert watchdogTimeout >= 0;

        log.info("Connecting to DTU: {}:{} (watchdog_timeout={})", dtuHost, dtuPort, watchdogTimeout);
        //noinspection ResultOfMethodCallIgnored
        Observable.create(emitter -> {
                    dtuClient.setListener(new DtuClientListenerExecutorWrapper(dtuListener, executor));
                    dtuClient.connect(dtuHost, dtuPort, watchdogTimeout);
                    emitter.onNext(1);
                })
                .retryWhen(RxUtils.retryPredicate(
                        e -> {
                            log.warn("DTU connection error: " + e.getMessage());
                            return true; //e instanceof ConnectTimeoutException;
                        }, -1,
                        count -> Math.min((long) Math.pow(2, count - 1), 300), TimeUnit.SECONDS))
                .blockingFirst();
        log.info("DTU connection success");
    }

    private void initMqttConnection() {
        log.info("Connecting to MQTT: {}", connectionConfigProvider.getConnectionUri());

        Observable.create(emitter -> {
                    mqttClient.setCallback(mqttCallback);
                    mqttClient.connect(connectionConfigProvider.getConnectionOptions());
                    emitter.onNext(1);
                })
                .retryWhen(RxUtils.retryPredicate(e -> {
                            log.warn("Mqtt connection error: " + e.getMessage());
                            log.warn("Retrying...");
                            return true;
                        }, -1,
                        count -> Math.min((long) Math.pow(2, count - 1), 300), TimeUnit.SECONDS))
                .blockingFirst();

        log.info("MQTT connection success");

        beanManager.fireEvent(new MqttConnectedEvent(connectionConfigProvider.getConnectionUri()));
    }
}




