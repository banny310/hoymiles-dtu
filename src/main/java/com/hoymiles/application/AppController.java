package com.hoymiles.application;

import com.hoymiles.domain.AutodiscoveryService;
import com.hoymiles.domain.IDtuRepository;
import com.hoymiles.domain.IMqttRepository;
import com.hoymiles.domain.MetricsService;
import com.hoymiles.domain.event.RealDataEvent;
import com.hoymiles.domain.model.AppInfo;
import com.hoymiles.domain.model.AppMode;
import com.hoymiles.infrastructure.dtu.DtuCommandBuilder;
import com.hoymiles.infrastructure.mqtt.MqttConnectedEvent;
import com.hoymiles.infrastructure.mqtt.MqttSendException;
import com.hoymiles.infrastructure.protos.GetConfig;
import com.hoymiles.infrastructure.protos.SetConfig;
import com.typesafe.config.Config;
import io.reactivex.rxjava3.core.Observable;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Dependent
@Log4j2
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class AppController {
    private final Config config;
    private final IDtuRepository dtuRepository;
    private final IMqttRepository mqttRepository;
    private final AutodiscoveryService autodiscoveryService;
    private final MetricsService metricsService;
    private final DtuCommandBuilder dtuCommand;

    private boolean pvAutodiscoverySent = false;

    @Handler
    public Void handleMqttConnected(@Observes @Priority(1) @NotNull MqttConnectedEvent event) {
        log.info("Successful connected to {}", event.getConnectionUri());

        log.info("Sending online state...");
        mqttRepository.sendOnlineState();
        return null;
    }

    @Handler
    public Void handleSolarData(@Observes @Priority(1) @NotNull RealDataEvent command) {
        log.info("Incoming new metrics");
        try {
            if (!pvAutodiscoverySent) {
                autodiscoveryService.registerPvAutodiscovery(command.getRealData().getPanels());
                pvAutodiscoverySent = true;
            }

            mqttRepository.sendRealData(command.getRealData());
        } catch (MqttSendException e) {
            log.error("Cannot send realData", e);
        }
        return null;
    }

    public void start() {
        log.info("Getting AppInfo from DTU...");
        AppInfo appInfo = dtuRepository.getAppInfo();

        log.info("DTU: hw={}, sw={}", appInfo.getDtuInfo().getDtuHw(), appInfo.getDtuInfo().getDtuSw());

        log.info("Sending autodiscovery...");
        autodiscoveryService.registerHomeAssistantAutodiscovery(appInfo);

        AppMode mode = AppMode.valueOf(config.getString("app.mode").toUpperCase());
        switch (mode) {
            case ACTIVE:
                Observable.create(emitter -> {
                    log.info("Pooling metrics...");
                    Observable.interval(config.getInt("app.mode_active.pull_interval"), TimeUnit.MILLISECONDS)
                            .map(time -> metricsService.getRealData(appInfo))
                            .subscribe(realData -> {
                                log.info("Sending realdata");
                                if (realData != null)
                                    mqttRepository.sendRealData(realData);
                            }, emitter::onError);
                }).blockingSubscribe();
                break;

            case PASSIVE:
                log.info("Gathering configuration...");
                GetConfig.GetConfigReq dtuConfig = dtuRepository.getConfiguration();

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
                        SetConfig.SetConfigReq confReq = dtuRepository.setConfiguration(confCmd);
                    }
                }
                break;

            default:
                throw new IllegalArgumentException(String.format("Unsupported mode: %s", mode));
        }
    }
}
