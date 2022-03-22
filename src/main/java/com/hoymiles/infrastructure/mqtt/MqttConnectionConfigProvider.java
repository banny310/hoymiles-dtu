package com.hoymiles.infrastructure.mqtt;

import com.typesafe.config.Config;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

@Dependent
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class MqttConnectionConfigProvider {

    private final Config config;

    public String getConnectionUri() {
        return String.format("tcp://%s:%d", config.getString("mqtt.host"), config.getInt("mqtt.port"));
    }

    public MqttConnectOptions getConnectionOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setKeepAliveInterval(60);
        options.setCleanSession(true);
        options.setConnectionTimeout(5);
        options.setUserName(config.getString("mqtt.username"));
        options.setPassword(config.getString("mqtt.password").toCharArray());
        options.setWill("hoymiles-dtu/bridge/state", "offline" .getBytes(), 1, true);
        return options;
    }
}
