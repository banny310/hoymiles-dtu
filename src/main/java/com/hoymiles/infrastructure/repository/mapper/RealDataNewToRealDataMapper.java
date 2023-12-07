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
public class RealDataNewToRealDataMapper implements GenericMapper<RealDataNew.RealReqDTO, RealData> {
    @Override
    public RealData map(RealDataNew.RealReqDTO src) {
        return RealData.builder()
                .dtuSn(src.getDtuSn().toString(StandardCharsets.ISO_8859_1))
                .powerTotal(src.getSgsDatasList().stream().map(v -> (float) v.getP() / 10f).reduce(0f, Float::sum))
                .energyTotal(src.getPvDatasList().stream().map(RealDataNew.PvMO::getEt).reduce(0, Integer::sum))
                .energyToday(src.getPvDatasList().stream().map(RealDataNew.PvMO::getEd).reduce(0, Integer::sum))
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
                                .powerFactor((float) src1.getPf() / 1000f)
                                .temp((float) (short)src1.getTemp() / 10f)
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
                .meters(src.getMeterDatasList().stream().map(
                        src2 -> RealData.MeterMO.builder()
                                .sn( String.valueOf(src2.getSn()).length() != 12 ? DeviceUtils.decToHex(String.valueOf(src2.getSn())) : String.valueOf(src2.getSn()).toUpperCase())
                                .time(src.getTime())
                                .type(src2.getType())
                                .ctAmps(getCtAmps(String.valueOf(src2.getSn())))
                                .powerA((float) src2.getPTta() * 10f)
                                .powerB((float) src2.getPTtb() * 10f)
                                .powerC((float) src2.getPTtc() * 10f)
                                .powerTotal((float) src2.getPTt() * 10f)
                                .voltageA((float) src2.getUA() / 100f)
                                .voltageB((float) src2.getUB() / 100f)
                                .voltageC((float) src2.getUC() / 100f)
                                .currentA((float) src2.getIA() / 100f)
                                .currentB((float) src2.getIB() / 100f)
                                .currentC((float) src2.getIC() / 100f)
                                .energyImportedA((float) src2.getEpTta() * 10f)
                                .energyImportedB((float) src2.getEpTtb() * 10f)
                                .energyImportedC((float) src2.getEpTtc() * 10f)
                                .energyImportedTotal((float) src2.getEpTt() * 10f)
                                .energyExportedA((float) src2.getEnTta() * 10f)
                                .energyExportedB((float) src2.getEnTtb() * 10f)
                                .energyExportedC((float) src2.getEnTtc() * 10f)
                                .energyExportedTotal((float) src2.getEnTt() * 10f)
                                .powerFactorA((float) src2.getPfA() / 1000f)
                                .powerFactorB((float) src2.getPfB() / 1000f)
                                .powerFactorC((float) src2.getPfC() / 1000f)
                                .powerFactorTotal((float) src2.getPfTt() / 1000f)
                                .build()
                        ).collect(Collectors.toList()))
                .build();
                
    }

        private String getCtAmps(String sn) {
                if (sn.length() != 12) {
                        sn = DeviceUtils.decToHex(sn).toUpperCase();
                    } else {
                        sn = sn.toUpperCase();
                    }

                if (sn.startsWith("10C011")) {
                                return "60";
                        } else if (sn.startsWith("10C013") || sn.startsWith("10C014")) {
                                return "80";
                        } else if (sn.startsWith("10C015") || sn.startsWith("10C016")) {
                                return "100";
                        } else if (sn.startsWith("10C017")) {
                                return "250";
                        }
                        return "0";
        }
}
