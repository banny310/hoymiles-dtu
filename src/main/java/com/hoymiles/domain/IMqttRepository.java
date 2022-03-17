package com.hoymiles.domain;

import com.hoymiles.domain.model.RealData;

public interface IMqttRepository {
    void sendOnlineState();

    void sendHomeAssistantConfig(String key, byte[] payload);

    void sendRealData(RealData realData);
}
