package com.hoymiles.infrastructure.repository.mapper;

import com.hoymiles.domain.model.RealData;
import com.hoymiles.infrastructure.GenericMapper;
import com.hoymiles.infrastructure.dtu.utils.DeviceUtils;
import com.hoymiles.infrastructure.protos.RealDataNew;
import jakarta.enterprise.context.Dependent;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Collectors;

@Dependent
public class Msg8717ToRealDataMapper implements GenericMapper<RealDataNew.Msg8717, RealData> {
    @Override
    public RealData map(RealDataNew.Msg8717 src) {
        return RealData.builder()
                .dtuSn(src.getDtuSn().toString(StandardCharsets.ISO_8859_1))
                .time(LocalDateTime.ofInstant(Instant.ofEpochSecond(src.getTime()), ZoneId.systemDefault()))
                .inverters(src.getSgsDatasList().stream().map(
                        src1 -> RealData.SGSMO.builder()
                                .sn(DeviceUtils.decToHex(String.valueOf(src1.getSn())))
                                .time(src.getTime())
                                .gridVoltage((float) src1.getV() / 10f)
                                .gridFreq((float) src1.getFreq() / 100f)
                                .gridPower((float) src1.getP() / 10f)
                                .gridReactivePower((float) src1.getQ() / 10f)
                                .gridCurrent((float) src1.getI() / 100f)
                                .powerFactor((float) src1.getPf() / 100f)
                                .temp((float) src1.getTemp() / 10f)
                                .link(src1.getLink())
                                .build()
                ).collect(Collectors.toList()))
                .panels(src.getPvDatasList().stream().map(
                        src2 -> RealData.PvMO.builder()
                                .sn(DeviceUtils.decToHex(String.valueOf(src2.getSn())))
                                .time(src.getTime())
                                .port(src2.getPi())
                                .voltage((float) src2.getV() / 10f)
                                .current((float) src2.getI() / 100f)
                                .power((float) src2.getP() / 10f)
                                .energyTotal(src2.getEt())
                                .energyToday(src2.getEd())
                                .build()
                ).collect(Collectors.toList()))
                .build();
    }
}
