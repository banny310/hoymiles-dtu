package com.hoymiles.domain;

import com.hoymiles.domain.model.AppInfo;
import com.hoymiles.domain.model.RealData;
import com.hoymiles.infrastructure.protos.GetConfig;
import com.hoymiles.infrastructure.protos.SetConfig;
import org.jetbrains.annotations.NotNull;

public interface IDtuRepository {
    AppInfo getAppInfo();

    RealData getRealData(@NotNull AppInfo appInfo);

    void sendRealDataReq(@NotNull AppInfo appInfo, int packetNum);

    GetConfig.GetConfigReq getConfiguration();

    SetConfig.SetConfigReq setConfiguration(SetConfig.SetConfigRes config);
}
