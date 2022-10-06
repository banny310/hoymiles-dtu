package com.hoymiles.infrastructure.dtu;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
public class DtuClientListenerExecutorWrapper implements DtuClient.Listener {
    private final DtuClient.Listener listener;
    private final ExecutorService executor;

    @Override
    public void onEvent(DtuMessage dtuMessage) {
        executor.execute(() -> listener.onEvent(dtuMessage));
    }

    @Override
    public void onError(Throwable throwable) {
        executor.execute(() -> listener.onError(throwable));
    }

    @Override
    public void onConnectionLost(Throwable cause) {
        executor.execute(() -> listener.onConnectionLost(cause));
    }
}
