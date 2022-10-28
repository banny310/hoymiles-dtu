package com.hoymiles.infrastructure.dtu;

import com.google.protobuf.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DtuMessage {
    private final int code;
    private final Message proto;
}
