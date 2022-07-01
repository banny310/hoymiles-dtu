package com.hoymiles.infrastructure.dtu;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DtuConnectionLostEvent {
    private final Throwable cause;
}
