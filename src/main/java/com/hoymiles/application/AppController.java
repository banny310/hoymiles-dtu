package com.hoymiles.application;

import com.hoymiles.domain.AutodiscoveryService;
import com.hoymiles.domain.IDtuRepository;
import com.hoymiles.domain.IMqttRepository;
import com.hoymiles.domain.MetricsService;
import com.hoymiles.domain.event.RealDataEvent;
import com.hoymiles.domain.model.AppInfo;
import com.hoymiles.domain.model.AppMode;
import com.hoymiles.domain.model.RealData;
import com.hoymiles.infrastructure.dtu.DtuCommandBuilder;
import com.hoymiles.infrastructure.mqtt.MqttConnectedEvent;
import com.hoymiles.infrastructure.mqtt.MqttSendException;
import com.hoymiles.infrastructure.protos.GetConfig;
import com.hoymiles.infrastructure.protos.SetConfig;
import com.typesafe.config.Config;
import io.reactivex.rxjava3.core.Observable;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
@Log4j2
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class AppController {
    private final Config config;
    private final IDtuRepository dtuRepository;
    private final IMqttRepository mqttRepository;
    private final AutodiscoveryService autodiscoveryService;
    private final MetricsService metricsService;
    private final DtuCommandBuilder dtuCommand;

    // WARNING: not stateless!!!
    private boolean pvAutodiscoverySent = false;
    private RealData lastRealData;

    @Handler
    public Void handleMqttConnected(@Observes @Priority(1) @NotNull MqttConnectedEvent event) {
        log.info("Successful connected to {}", event.getConnectionUri());

        log.info("Sending online state...");
        mqttRepository.sendOnlineState();
        return null;
    }

    /**
        NOTICE: DTU in large installations divides data in two separate messages
        One of them contains inverters and some panels, other one contains only panels
        For example: installation with 6 inverters and 24 panels
        Message 1: 6 inverters, 8 panels
        Message 2: 0 inverters, 16 panels
     */
    @Handler
    public Void handleSolarData(@Observes @Priority(1) @NotNull RealDataEvent command) {
        log.info("Incoming new metrics");
        try {
            RealData data = command.getRealData();
            if (!pvAutodiscoverySent) {
                autodiscoveryService.registerPvAutodiscovery(data.getPanels());
                pvAutodiscoverySent = true;
            }

            int nextPacketNum = Optional.ofNullable(lastRealData)
                    .map(realData -> realData.getPacketNum() + 1)
                    .orElse(0);
            log.info("Packet received: num={}, count={}, expected={}", data.getPacketNum(), data.getPacketCount(), nextPacketNum);

            if (data.getPacketNum() == nextPacketNum) {
                // merge incoming packet with previously stored
                lastRealData = Optional.ofNullable(lastRealData)
                        .map(realData -> realData.merge(data))
                        .orElse(data);

                if (data.getPacketNum() == data.getPacketCount() - 1) {
                    log.info("Last packed received. Sending to mqtt...");
                    mqttRepository.sendRealData(Optional.ofNullable(lastRealData).orElse(data));
                    lastRealData = null;
                }
            } else {
                log.info("Packet number mismatch");
                lastRealData = null;
            }
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
