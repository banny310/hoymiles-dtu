package com.hoymiles.infrastructure.repository;

import com.google.gson.Gson;
import com.hoymiles.domain.IMqttRepository;
import com.hoymiles.domain.InfrastructureException;
import com.hoymiles.domain.model.RealData;
import com.hoymiles.infrastructure.repository.dto.RealDataDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

@Dependent
@Log4j2
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class MqttRepository implements IMqttRepository {

    private final IMqttClient mqttClient;
    private final Gson gson;
    private final ModelMapper modelMapper;

    @Override
    public void sendOnlineState() {
        try {
            mqttClient.publish("hoymiles-solar/bridge/state", "online".getBytes(), 1, true);
        } catch (MqttException e) {
            throw new InfrastructureException("Mqtt exception", e);
        }
    }

    public void sendHomeAssistantConfig(String key, byte[] payload) {
        try {
            String topic = String.format("homeassistant/sensor/hoymiles-solar/%s/config", key);
            mqttClient.publish(topic, payload, 0, true);
        } catch (MqttException e) {
            throw new InfrastructureException("Mqtt exception", e);
        }
    }

    @Override
    public void sendRealData(@NotNull RealData realData) {
        try {
            String dtuId = String.format("dtu_%s", realData.getDtuSn());
            RealDataDTO dto = modelMapper.map(realData, RealDataDTO.class);
            mqttClient.publish("hoymiles-solar/" + dtuId, gson.toJson(dto).getBytes(), 0, false);
        } catch (MqttException e) {
            throw new InfrastructureException("Mqtt exception", e);
        }
    }
}
