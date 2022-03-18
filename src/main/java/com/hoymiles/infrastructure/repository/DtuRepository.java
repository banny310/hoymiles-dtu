package com.hoymiles.infrastructure.repository;

import com.google.protobuf.Message;
import com.hoymiles.domain.IDtuRepository;
import com.hoymiles.domain.event.RealDataEvent;
import com.hoymiles.domain.model.AppInfo;
import com.hoymiles.domain.model.RealData;
import com.hoymiles.infrastructure.dtu.DtuClient;
import com.hoymiles.infrastructure.dtu.DtuCommandBuilder;
import com.hoymiles.infrastructure.dtu.DtuMessage;
import com.hoymiles.infrastructure.dtu.utils.RxUtils;
import com.hoymiles.infrastructure.protos.APPInformationData;
import com.hoymiles.infrastructure.protos.RealDataNew;
import com.hoymiles.infrastructure.repository.mapper.APPInformationData2AppInfoMapper;
import com.hoymiles.infrastructure.repository.mapper.Msg8716ToRealDataMapper;
import com.hoymiles.infrastructure.repository.mapper.RealDataNewToRealDataMapper;
import com.typesafe.config.Config;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

@Dependent
@Log4j2
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class DtuRepository implements IDtuRepository {
    private final BeanManager beanManager;
    private final Config config;
    private final DtuClient dtuClient;
    private final DtuCommandBuilder dtuCommand;
    private final APPInformationData2AppInfoMapper appInformationData2AppInfoMapper;
    private final RealDataNewToRealDataMapper realDataNew2RealDataMapper;
    private final Msg8716ToRealDataMapper msg8716ToRealDataMapper;

    private final SpreadsheetWriter spreadsheetWriter;

    public Void handle(@Observes @NotNull DtuMessage event) {

        if (config.getBoolean("app.store_messages_in_excel")) {
            spreadsheetWriter.write(event.getCode(), event.getMessage());
        }

        switch (event.getCode()) {
            case 8716:
                RealData realData = msg8716ToRealDataMapper.map((RealDataNew.Msg8716) event.getMessage());
                beanManager.fireEvent(new RealDataEvent(realData));
                break;
        }

        return null;
    }

    @Override
    public AppInfo getAppInfo() {
        APPInformationData.APPInfoDataResDTO cmd = dtuCommand.appInfoBuilder().build();
        APPInformationData.APPInfoDataReqDTO appInfoDTO = dtuClient.command(cmd, APPInformationData.APPInfoDataReqDTO.class)
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.trampoline())
                .timeout(5, TimeUnit.SECONDS)
                .retry(RxUtils.retryPredicate(3))
                .blockingFirst();

        return appInformationData2AppInfoMapper.map(appInfoDTO);
    }

    @Override
    public RealData getRealData(@NotNull AppInfo appInfo) {
        if (appInfo.getDtuInfo().getDtuSw() >= 512) {
            RealDataNew.RealReqDTO realReqDTO = dtuClient.command(dtuCommand.realDataXBuilder().build(), RealDataNew.RealReqDTO.class)
                    .observeOn(Schedulers.newThread())
                    .subscribeOn(Schedulers.trampoline())
                    .timeout(5, TimeUnit.SECONDS)
                    .retry(RxUtils.retryPredicate(3))
                    .blockingFirst();
            return realDataNew2RealDataMapper.map(realReqDTO);
        } else {
//            com.hoymiles.protos.RealData.RealDataReqDTO realReqDTO = dtuClient.command(dtuCommand.realDataBuilder().build(), com.hoymiles.protos.RealData.RealDataReqDTO.class)
//                    .observeOn(Schedulers.newThread())
//                    .subscribeOn(Schedulers.trampoline())
//                    .timeout(5, TimeUnit.SECONDS)
//                    .retry(RxUtils.retryPredicate(3))
//                    .blockingFirst();
            throw new NotImplementedException("Device with sw_version < 512 is currently not supported");
        }
    }
}
