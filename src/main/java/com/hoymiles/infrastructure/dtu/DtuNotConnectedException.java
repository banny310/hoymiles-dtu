package com.hoymiles.infrastructure.dtu;

public class DtuNotConnectedException extends IllegalStateException {
    DtuNotConnectedException() {
        super("Not connected");
    }
}
