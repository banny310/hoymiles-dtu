package com.hoymiles.domain;

import com.hoymiles.domain.model.AppInfo;
import com.hoymiles.domain.model.RealData;
import org.jetbrains.annotations.NotNull;

public interface IDtuRepository {
    AppInfo getAppInfo();

    RealData getRealData(@NotNull AppInfo appInfo);
}
