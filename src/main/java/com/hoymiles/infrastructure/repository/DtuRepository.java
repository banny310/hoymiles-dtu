package com.hoymiles.infrastructure.repository;

import com.hoymiles.domain.IDtuRepository;
import com.hoymiles.domain.model.AppInfo;
import com.hoymiles.domain.model.RealData;
import com.hoymiles.infrastructure.dtu.DtuClient;
import com.hoymiles.infrastructure.dtu.DtuCommandBuilder;
import com.hoymiles.infrastructure.dtu.NoHandlerException;
import com.hoymiles.infrastructure.dtu.utils.RxUtils;
import com.hoymiles.infrastructure.protos.APPInformationData;
import com.hoymiles.infrastructure.protos.GetConfig;
import com.hoymiles.infrastructure.protos.RealDataNew;
import com.hoymiles.infrastructure.protos.SetConfig;
import com.hoymiles.infrastructure.repository.mapper.APPInformationData2AppInfoMapper;
import com.hoymiles.infrastructure.repository.mapper.RealDataNewToRealDataMapper;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.enterprise.context.Dependent;
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
    private final DtuClient dtuClient;
    private final DtuCommandBuilder dtuCommand;
    private final APPInformationData2AppInfoMapper appInformationData2AppInfoMapper;
    private final RealDataNewToRealDataMapper realDataNew2RealDataMapper;

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
            throw new NotImplementedException("Device with sw_version < 512 is currently not supported");
        }
    }

    @Override
    public void sendRealDataReq(@NotNull AppInfo appInfo, int packetNum) {
        if (appInfo.getDtuInfo().getDtuSw() >= 512) {
            try {
                dtuClient.send(dtuCommand.realDataXBuilder().setCp(packetNum).build());
            } catch (NoHandlerException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new NotImplementedException("Device with sw_version < 512 is currently not supported");
        }
    }

    @Override
    public GetConfig.GetConfigReq getConfiguration() {
        GetConfig.GetConfigRes cmd = dtuCommand.getConfigBuilder().build();
        return dtuClient.command(cmd, GetConfig.GetConfigReq.class).blockingFirst();
    }

    @Override
    public SetConfig.SetConfigReq setConfiguration(SetConfig.SetConfigRes config) {
        return dtuClient.command(config, SetConfig.SetConfigReq.class).blockingFirst();
    }
}
