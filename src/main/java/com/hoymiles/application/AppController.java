package com.hoymiles.application;

import com.hoymiles.domain.AutodiscoveryService;
import com.hoymiles.domain.IDtuRepository;
import com.hoymiles.domain.IMqttRepository;
import com.hoymiles.domain.MetricsService;
import com.hoymiles.domain.event.RealDataEvent;
import com.hoymiles.domain.model.AppInfo;
import com.hoymiles.domain.model.AppMode;
import com.hoymiles.domain.model.RealData;
import com.hoymiles.infrastructure.dtu.DtuClient;
import com.hoymiles.infrastructure.dtu.DtuCommandBuilder;
import com.hoymiles.infrastructure.protos.GetConfig;
import com.hoymiles.infrastructure.protos.SetConfig;
import com.typesafe.config.Config;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Dependent
@Log4j2
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class AppController {
    private final ScheduledExecutorService executor;
    private final Config config;
    private final IDtuRepository dtuRepository;
    private final IMqttRepository mqttRepository;
    private final AutodiscoveryService autodiscoveryService;
    private final MetricsService metricsService;
    private final DtuCommandBuilder dtuCommand;
    private final DtuClient dtuClient;

    private boolean pvAutodiscoverySent = false;

    public Void handle(@Observes @Priority(1) @NotNull RealDataEvent command) {
        log.info("Incoming new metrics");
        sendRealData(command.getRealData());
        return null;
    }

    public void start() throws InterruptedException {
        log.debug("Sending online state...");
        mqttRepository.sendOnlineState();

        log.debug("Getting AppInfo from DTU...");
        AppInfo appInfo = dtuRepository.getAppInfo();

        log.info("Sending autodiscovery...");
        autodiscoveryService.registerHomeAssistantAutodiscovery(appInfo);

        AppMode mode = AppMode.valueOf(config.getString("app.mode").toUpperCase());
        switch (mode) {
            case ACTIVE:
                int delay = config.getInt("app.mode_active.pull_interval");
                Runnable job = new Runnable() {
                    @Override
                    public void run() {
                        log.info("Pooling metrics...");
                        Optional.ofNullable(metricsService.getRealData(appInfo))
                                .ifPresent(realData -> sendRealData(realData));
                        executor.schedule(this, delay, TimeUnit.MILLISECONDS);
                    }
                };

                executor.execute(job);
                break;

            case PASSIVE:
                log.info("Gathering configuration...");
                GetConfig.GetConfigRes cmd = dtuCommand.getConfigBuilder().build();
                GetConfig.GetConfigReq dtuConfig = dtuClient.command(cmd, GetConfig.GetConfigReq.class).blockingFirst();

                boolean setServerSendTime = config.getBoolean("app.mode_passive.set_server_send_time");
                if (setServerSendTime) {
                    int serverSendTime = config.getInt("app.mode_passive.server_send_time");
                    assert serverSendTime > 0;
                    log.info("serverSendTime: expected={}, current={}", serverSendTime, dtuConfig.getServerSendTime());
                    if (serverSendTime != dtuConfig.getServerSendTime()) {
                        SetConfig.SetConfigRes confCmd = dtuCommand
                                .setConfigBuilder(dtuConfig.getDtuSn().toString(StandardCharsets.ISO_8859_1))
                                .setServerSendTime(serverSendTime)
                                .build();

                        log.info("changing 'serverSendTime' to {}", serverSendTime);
                        SetConfig.SetConfigReq confReq = dtuClient.command(confCmd, SetConfig.SetConfigReq.class).blockingFirst();
                    }
                }
                break;
        }

//            Observable.create(emitter -> {
//                log.info("Pooling metrics...");
//                Observable.interval(config.getInt("app.mode_active.pull_interval"), TimeUnit.MILLISECONDS)
//                        .map(time -> metricsService.getRealData(appInfo))
//                        .subscribe(realData -> {
//                            log.info("Sending realdata");
//                            if (realData != null)
//                                mqttRepository.sendRealData(realData);
//                        }, emitter::onError);
//            }).blockingSubscribe();

        log.info("Waiting for incoming messages...");
        while (executor.awaitTermination(10, TimeUnit.SECONDS)) {}
    }

    private void sendRealData(RealData realData) {
        if (!pvAutodiscoverySent) {
            autodiscoveryService.registerPvAutodiscovery(realData.getPanels());
            pvAutodiscoverySent = true;
        }

        mqttRepository.sendRealData(realData);
    }
}
