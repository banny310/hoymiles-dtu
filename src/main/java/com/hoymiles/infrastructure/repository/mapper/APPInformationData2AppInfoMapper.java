package com.hoymiles.infrastructure.repository.mapper;

import com.hoymiles.domain.model.AppInfo;
import com.hoymiles.infrastructure.GenericMapper;
import com.hoymiles.infrastructure.dtu.utils.DeviceUtils;
import com.hoymiles.infrastructure.protos.APPInformationData;
import jakarta.enterprise.context.Dependent;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Collectors;

@Dependent
public class APPInformationData2AppInfoMapper implements GenericMapper<APPInformationData.APPInfoDataReqDTO, AppInfo> {
    @Override
    public AppInfo map(APPInformationData.APPInfoDataReqDTO src) {
        return AppInfo.builder()
                .dtuSn(src.getDtuSn().toString(StandardCharsets.ISO_8859_1))
                .time(LocalDateTime.ofInstant(Instant.ofEpochSecond(src.getTime()), ZoneId.systemDefault()))
                .dtuInfo(AppInfo.DtuInfo.builder()
                        .dtuSw(src.getMAPPDtuInfo().getDtuSw())
                        .dtuHw(src.getMAPPDtuInfo().getDtuHw())
                        .build())
                .sgsInfo(
                        src.getMAPPpvInfoList().stream().map(src1 ->
                                AppInfo.SgsInfo.builder()
                                        .invSn(DeviceUtils.decToHex(String.valueOf(src1.getPvSn())))
                                        .invSw(src1.getPvSw())
                                        .invHw(src1.getPvHw())
                                        .build()
                        ).collect(Collectors.toList()))
                .meterInfo(
                        src.getMAPPMeterInfoList().stream().map(src1 ->
                                AppInfo.MeterInfo.builder()
                                        .meterSn(String.valueOf(src1.getMeterSn()).length() != 12 ? DeviceUtils.decToHex(String.valueOf(src1.getMeterSn())) : String.valueOf(src1.getMeterSn()).toUpperCase())
                                        .deviceKind(src1.getDeviceKind())
                                        .meterModel(src1.getMeterModel())
                                        .meterCt(src1.getMeterCt())
                                        .comWay(src1.getComWay())
                                        .accessMode(src1.getAccessMode())
                                        .swVsn(src1.getSwVsn())
                                        .build()
                        ).collect(Collectors.toList()))
                .build();
    }
}
