package com.hoymiles.infrastructure.repository.mapper;

import com.hoymiles.domain.model.AppInfo;
import com.hoymiles.infrastructure.GenericMapper;
import com.hoymiles.infrastructure.dtu.utils.DeviceUtils;
import com.hoymiles.infrastructure.protos.APPInformationData;
import jakarta.enterprise.context.Dependent;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Dependent
public class APPInformationData2AppInfoMapper implements GenericMapper<APPInformationData.APPInfoDataReqDTO, AppInfo> {
    @Override
    public AppInfo map(APPInformationData.APPInfoDataReqDTO src) {
        return AppInfo.builder()
                .dtuSn(src.getDtuSn().toString(StandardCharsets.ISO_8859_1))
                .dtuInfo(AppInfo.DtuInfo.builder()
                        .dtuSw(src.getMAPPDtuInfo().getDtuSw())
                        .dtuHw(src.getMAPPDtuInfo().getDtuHw())
                        .build())
                .sgsInfo(
                        src.getMAPPpvInfoList().stream().map(src1 ->
                                AppInfo.SgsInfo.builder()
                                        .sn(DeviceUtils.decToHex(String.valueOf(src1.getPvSn())))
                                        .build()
                        ).collect(Collectors.toList()))
                .build();
    }
}
