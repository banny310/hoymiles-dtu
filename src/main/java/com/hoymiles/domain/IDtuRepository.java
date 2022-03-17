package com.hoymiles.domain;

import com.google.protobuf.Message;
import com.hoymiles.domain.model.AppInfo;
import com.hoymiles.domain.model.RealData;
import io.reactivex.rxjava3.core.Observable;
import org.jetbrains.annotations.NotNull;

public interface IDtuRepository {
    AppInfo getAppInfo();

    RealData getRealData(@NotNull AppInfo appInfo);

    <T extends Message> Observable<T> command(Message message, Class<T> responseClazz);
}
