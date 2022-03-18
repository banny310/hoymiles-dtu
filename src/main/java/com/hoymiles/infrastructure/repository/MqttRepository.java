package com.hoymiles.infrastructure.repository;

import com.google.gson.Gson;
import com.hoymiles.domain.IMqttRepository;
import com.hoymiles.domain.InfrastructureException;
import com.hoymiles.domain.model.RealData;
import com.hoymiles.infrastructure.repository.dto.DtuRealDataDTO;
import com.hoymiles.infrastructure.repository.dto.InvRealDataDTO;
import com.hoymiles.infrastructure.repository.dto.PvRealDataDTO;
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

    private boolean pvAutodiscoverySent = false;

    @Override
    public void sendOnlineState() {
        try {
            mqttClient.publish("hoymiles-dtu/bridge/state", "online".getBytes(), 1, true);
        } catch (MqttException e) {
            throw new InfrastructureException("Mqtt exception", e);
        }
    }

    public void sendHomeAssistantConfig(String key, byte[] payload) {
        try {
            String topic = String.format("homeassistant/sensor/hoymiles-dtu/%s/config", key);
            mqttClient.publish(topic, payload, 0, true);
        } catch (MqttException e) {
            throw new InfrastructureException("Mqtt exception", e);
        }
    }

    @Override
    public void sendRealData(@NotNull RealData realData) {
        try {
            String dtuId = String.format("dtu_%s", realData.getDtuSn());
            DtuRealDataDTO dto = modelMapper.map(realData, DtuRealDataDTO.class);
            mqttClient.publish("hoymiles-dtu/" + dtuId, gson.toJson(dto).getBytes(), 0, false);

            for (RealData.SGSMO sgsmo: realData.getInverters()) {
                String invId = String.format("inv_%s", sgsmo.getSn());
                InvRealDataDTO invDto = modelMapper.map(sgsmo, InvRealDataDTO.class);
                mqttClient.publish("hoymiles-dtu/" + invId, gson.toJson(invDto).getBytes(), 0, false);
            }

            for (RealData.PvMO pvmo: realData.getPanels()) {
                String pvId = String.format("pv_%s_%d", pvmo.getSn(), pvmo.getPosition());
                PvRealDataDTO pvDto = modelMapper.map(pvmo, PvRealDataDTO.class);
                mqttClient.publish("hoymiles-dtu/" + pvId, gson.toJson(pvDto).getBytes(), 0, false);
            }

        } catch (MqttException e) {
            throw new InfrastructureException("Mqtt exception", e);
        }
    }
}
