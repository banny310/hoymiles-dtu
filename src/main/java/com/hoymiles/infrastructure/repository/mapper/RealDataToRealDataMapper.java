package com.hoymiles.infrastructure.repository.mapper;

//@Dependent
//public class RealData2RealDataMapper implements GenericMapper<com.hoymiles.protos.RealData.RealDataReqDTO, RealData> {
//    @Override
//    public RealData map(com.hoymiles.protos.RealData.RealDataReqDTO src) {
//        return RealData.builder()
//                .dtuSn(src.getDtuSn().toString(StandardCharsets.ISO_8859_1))
//                .powerTotal(src.getMpvDatasList().stream().map(v -> (float) v.getGridP() / 10f).reduce(0f, Float::sum))
//                .energyTotal(src.getMpvDatasList().stream().map(com.hoymiles.protos.RealData.PvDataMO::getPvEnergyTotal).reduce(0, Integer::sum))
//                .energyDaily(src.getMpvDatasList().stream().map(com.hoymiles.protos.RealData.PvDataMO::getPvEnergy).reduce(0, Integer::sum))
//                .inverters(src.getMpvDatasList().stream().map(
//                                src1 -> RealData.SGSMO.builder()
//                                        .sn(DeviceUtils.decToHex(String.valueOf(src1.getPvSn())))
//                                        .gridVoltage((float) src1.getGridVol() / 10f)
//                                        .gridFreq((float) src1.getGridFreq() / 100f)
//                                        .gridPower((float) src1.getGridP() / 10f)
//                                        .gridReactivePower((float) src1.getGridQ() / 10f)
//                                        .gridCurrent((float) src1.getGridI() / 100f)
//                                        .powerFactor((float) src1.getGridPf() / 100f)
//                                        .temp((float) src1.getPvTemp() / 10f)
//                                        .build())
//                        .collect(Collectors.toList()))
//                .panels(src.getMRpDatasList().stream().map(
//                                src2 -> RealData.PvMO.builder()
//                                        .sn(DeviceUtils.decToHex(String.valueOf(src2.ge())))
//                                        .position(src2.getPi())
//                                        .voltage((float) src2.getV() / 10f)
//                                        .current((float) src2.getI() / 100f)
//                                        .power((float) src2.getP() / 10f)
//                                        .energyTotal(src2.getEt())
//                                        .energyDaily(src2.getEd())
//                                        .build())
//                        .collect(Collectors.toList()))
//                .build();
//    }
//}
