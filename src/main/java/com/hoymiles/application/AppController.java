package com.hoymiles.application;

import com.google.protobuf.Message;
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
import com.hoymiles.infrastructure.dtu.utils.DeviceUtils;
import com.hoymiles.infrastructure.mqtt.MqttConnectedEvent;
import com.hoymiles.infrastructure.mqtt.MqttSendException;
import com.hoymiles.infrastructure.protos.GetConfig;
import com.hoymiles.infrastructure.protos.NetworkInfo;
import com.hoymiles.infrastructure.protos.SetConfig;
import com.typesafe.config.Config;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    private final DtuClient dtuClient;

    // WARNING: not stateless!!!
    private AppInfo appInfo;
    private boolean pvAutodiscoverySent = false;
    private Disposable poolDisposable;

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
        RealData data = command.getRealData();
        log.info("Incoming new metrics: time={}", data.getTime());
        // make sure metrics are not older than 5 minutes
        if (!LocalDateTime.now().minusMinutes(5).isBefore(data.getTime())) {
            long minutes = ChronoUnit.MINUTES.between(data.getTime(), LocalDateTime.now());
            log.warn("Metrics are {} minutes old. Discarding.", minutes);
            return null;
        }

        try {
            if (!pvAutodiscoverySent) {
                autodiscoveryService.registerPvAutodiscovery(data.getPanels());
                pvAutodiscoverySent = true;
            }

            mqttRepository.sendRealData(data);
        } catch (MqttSendException e) {
            log.error("Cannot send realData", e);
        }
        return null;
    }

    public void start() {
        log.info("Getting AppInfo from DTU...");
        appInfo = dtuRepository.getAppInfo();


        log.info("DTU: hw={}, sw={}, time={}", appInfo.getDtuInfo().getDtuHw(), appInfo.getDtuInfo().getDtuSw(), appInfo.getTime());

        log.info("Sending autodiscovery...");
        autodiscoveryService.registerHomeAssistantAutodiscovery(appInfo);
        
        AppMode mode = AppMode.valueOf(config.getString("app.mode").toUpperCase());
        switch (mode) {
            case ACTIVE:
                int pullInterval = config.getInt("app_mode_active.pull_interval");
                metricsService.sendRealDataReq(appInfo, 0);
                poolDisposable = Observable.interval(pullInterval, TimeUnit.SECONDS)
                                .subscribe(tick -> metricsService.sendRealDataReq(appInfo, 0));

//                Message msg = dtuCommand.genericCommandBuilder()
//                        .setYmdHms(DeviceUtils.toByteString("2022-10-27 19:48:45"))
//                        .setPackageNow(0)
//                        .setTime(1666871325)
//                        .build();
//                dtuClient.send(new DtuMessage(8971, msg));

                break;

            case PASSIVE:
                log.info("Gathering configuration...");
                GetConfig.GetConfigReq dtuConfig = dtuRepository.getConfiguration();

                boolean setServerSendTime = config.getBoolean("app_mode_passive.set_server_send_time");
                if (setServerSendTime) {
                    int serverSendTime = config.getInt("app_mode_passive.server_send_time");
                    assert serverSendTime > 0;
                    log.info("serverSendTime: expected={}, current={}", serverSendTime, dtuConfig.getServerSendTime());
                    if (serverSendTime != dtuConfig.getServerSendTime()) {
                        SetConfig.SetConfigRes confCmd = dtuCommand
                                .setConfigBuilder(dtuConfig.getDtuSn().toString(StandardCharsets.ISO_8859_1))
                                .setServerSendTime(serverSendTime)
                                .setWifiSsid(dtuConfig.getWifiSsid())
                                .setWifiPassword(dtuConfig.getWifiPassword())
                                .build();

                        log.info("changing 'serverSendTime' to {}", serverSendTime);
                        SetConfig.SetConfigReq confReq = dtuRepository.setConfiguration(confCmd);
                    }
                }
                break;

            case NONE:
                // do nothing (just listen)
                Message message = NetworkInfo.NetworkInfoRes.newBuilder()
                        .setTime(DeviceUtils.getCurrentTime())
                        .setOffset(3600).build();
                dtuClient.send(message);
                break;

            default:
                throw new IllegalArgumentException(String.format("Unsupported mode: %s", mode));
        }
    }

    public void stop() {
        log.info("Stopping...");
        Optional.ofNullable(poolDisposable).ifPresent(Disposable::dispose);
    }
}
