package com.hoymiles.infrastructure.mqtt;

import com.hoymiles.domain.InfrastructureException;

public class MqttSendException extends InfrastructureException {
    public MqttSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
