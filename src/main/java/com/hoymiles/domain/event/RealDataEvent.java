package com.hoymiles.domain.event;

import com.hoymiles.domain.model.RealData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RealDataEvent {
    private final RealData realData;
}
