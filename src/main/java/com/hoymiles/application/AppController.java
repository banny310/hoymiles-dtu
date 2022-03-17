package com.hoymiles.application;

import com.hoymiles.domain.AutodiscoveryService;
import com.hoymiles.domain.IDtuRepository;
import com.hoymiles.domain.IMqttRepository;
import com.hoymiles.domain.MetricsService;
import com.hoymiles.domain.event.RealDataEvent;
import com.hoymiles.domain.model.AppInfo;
import com.hoymiles.infrastructure.dtu.DtuCommandBuilder;
import com.hoymiles.infrastructure.protos.GetConfig;
import com.typesafe.config.Config;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

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

    public Void handle(@Observes @Priority(1) @NotNull RealDataEvent command) {
        log.info("Incoming new metrics");
        mqttRepository.sendRealData(command.getRealData());
        return null;
    }

    public void start() {
        log.debug("Sending online state...");
        mqttRepository.sendOnlineState();

        log.debug("Getting AppInfo from DTU...");
        AppInfo appInfo = dtuRepository.getAppInfo();

        log.info("Sending autodiscovery...");
        autodiscoveryService.registerHomeAssistantAutodiscovery(appInfo);

        log.info("Waiting for incoming messages...");
//        Observable.create(emitter -> {
//            log.info("Pooling metrics...");
//            Observable.interval(config.getInt("app.pull_interval"), TimeUnit.MILLISECONDS)
//                    .map(time -> metricsService.getRealData(appInfo))
//                    .subscribe(realData -> {
//                        log.info("Sending realdata");
//                        if (realData != null)
//                            mqttRepository.sendRealData(realData);
//                    }, emitter::onError);
//        }).blockingSubscribe();

//        GetConfig.GetConfigRes cmd = dtuCommand.getConfigBuilder().build();
//        GetConfig.GetConfigReq config = dtuRepository.command(cmd, GetConfig.GetConfigReq.class).blockingFirst();

//        SetConfig.SetConfigRes cmd1 = dtuCommand
//                .setConfigBuilder(config)
//                .setMeterInterface(ByteString.copyFrom("NONE", StandardCharsets.ISO_8859_1))
//                .setMeterKind(ByteString.copyFrom("NONE", StandardCharsets.ISO_8859_1))
//                .setNetmodeSelect(1)
//                .build();
//        SetConfig.SetConfigReq config2 = dtuRepository.command(cmd1, SetConfig.SetConfigReq.class).blockingFirst();
    }
}
