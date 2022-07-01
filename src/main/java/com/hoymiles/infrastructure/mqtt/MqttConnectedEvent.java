package com.hoymiles.infrastructure.mqtt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MqttConnectedEvent {
    private final String connectionUri;
}
