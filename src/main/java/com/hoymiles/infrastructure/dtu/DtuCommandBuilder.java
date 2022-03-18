package com.hoymiles.infrastructure.dtu;

import com.google.protobuf.ByteString;
import com.hoymiles.infrastructure.dtu.utils.DateUtil;
import com.hoymiles.infrastructure.dtu.utils.DeviceUtils;
import com.hoymiles.infrastructure.protos.*;

import jakarta.enterprise.context.Dependent;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

@Dependent
public class DtuCommandBuilder {


    public CommandPB.CommandResDTO.Builder commandBuilder() {
        int time = DeviceUtils.getCurrentTime();
        CommandPB.CommandResDTO.Builder newBuilder = CommandPB.CommandResDTO.newBuilder();
        newBuilder.setTime(time);
        newBuilder.setAction(33);
        newBuilder.setData(DeviceUtils.toByteString(""));
        newBuilder.setTid(time);
        newBuilder.setDevKind(0);
        newBuilder.setPackageNub(1);
        newBuilder.setPackageNow(0);
        return newBuilder;
    }

    public GenericCommand.GenericCommandResDTO2.Builder genericCommand2Builder() {
        int time = DeviceUtils.getCurrentTime();
        GenericCommand.GenericCommandResDTO2.Builder newBuilder = GenericCommand.GenericCommandResDTO2.newBuilder();
        newBuilder.setYmdHms(DeviceUtils.toByteString(
                new SimpleDateFormat(DateUtil.DATE_FORMAT).format(new Date(System.currentTimeMillis()))
        ));
        newBuilder.setOffset(3600);
        newBuilder.setTime(time);
        return newBuilder;
    }

    public APPInformationData.APPInfoDataResDTO.Builder appInfoBuilder() {
        APPInformationData.APPInfoDataResDTO.Builder newBuilder = APPInformationData.APPInfoDataResDTO.newBuilder();
        newBuilder.setTime(DeviceUtils.getCurrentTime());
        newBuilder.setOffset(28800);
        newBuilder.setYmdHms(DeviceUtils.toByteString(
                new SimpleDateFormat(DateUtil.DATE_FORMAT).format(new Date(System.currentTimeMillis()))
        ));
        newBuilder.setErrCode(0);
        newBuilder.setPackageNow(0);
        return newBuilder;
    }

    public RealData.RealDataResDTO.Builder realDataBuilder() {
        RealData.RealDataResDTO.Builder newBuilder = RealData.RealDataResDTO.newBuilder();
        newBuilder.setTime(DeviceUtils.getCurrentTime());
        newBuilder.setOffset(28800);
        newBuilder.setYmdHms(DeviceUtils.toByteString(
                new SimpleDateFormat(DateUtil.DATE_FORMAT).format(new Date(System.currentTimeMillis())))
        );
        newBuilder.setErrCode(0);
        newBuilder.setPackageNow(0);
        return newBuilder;
    }

    public RealDataNew.RealResDTO.Builder realDataXBuilder() {
        RealDataNew.RealResDTO.Builder newBuilder = RealDataNew.RealResDTO.newBuilder();
        newBuilder.setTime(DeviceUtils.getCurrentTime());
        newBuilder.setOft(28800);
        newBuilder.setYmdHms(DeviceUtils.toByteString(
                new SimpleDateFormat(DateUtil.DATE_FORMAT).format(new Date(System.currentTimeMillis())))
        );
        newBuilder.setErrCode(0);
        newBuilder.setCp(0);
        return newBuilder;
    }

    public GetConfig.GetConfigRes.Builder getConfigBuilder() {
        return GetConfig.GetConfigRes.newBuilder()
                .setOffset(28800)
                .setTime(DeviceUtils.getCurrentTime());
    }

    public SetConfig.SetConfigRes.Builder setConfigBuilder(String dtuSn) {
        return SetConfig.SetConfigRes.newBuilder()
                .setTime(DeviceUtils.getCurrentTime())
                .setDtuSn(ByteString.copyFrom(dtuSn, StandardCharsets.ISO_8859_1))
                .setOffset(28800);
    }

    public SetConfig.SetConfigRes.Builder setConfigBuilder(GetConfig.GetConfigReq config) {
        return SetConfig.SetConfigRes.newBuilder()
                .setTime(DeviceUtils.getCurrentTime())
                .setDtuSn(config.getDtuSn())
                .setOffset(config.getOffset())
                .setInvType(config.getInvType())
                .setLockPassword(config.getLockPassword())
                .setLockTime(config.getLockTime())
                .setLimitPowerMyPower(config.getLimitPowerMyPower())
                .setZeroExport433Addr(config.getZeroExport433Addr())
                .setMeterKind(config.getMeterKind())
                .setMeterInterface(config.getMeterInterface())
                .setZeroExportEnable(config.getZeroExportEnable())
                .setNetmodeSelect(config.getNetmodeSelect())
                .setApnSet(config.getApnSet())
                .setApnName(config.getApnName())
                .setApnPassword(config.getApnPassword())
                .setChannelSelect(config.getChannelSelect())
                .setServerSendTime(config.getServerSendTime())
                .setWifiSsid(config.getWifiSsid())
                .setWifiPassword(config.getWifiPassword())
                .setServerDomainName(config.getServerDomainName())
                .setServerPort(config.getServerPort())
                .setAccessModel(config.getAccessModel())
                .setMac0(config.getMac0())
                .setMac1(config.getMac1())
                .setMac2(config.getMac2())
                .setMac3(config.getMac3())
                .setMac4(config.getMac4())
                .setMac5(config.getMac5())
                .setDhcpSwitch(config.getDhcpSwitch())
                .setIpAddr0(config.getIpAddr0())
                .setIpAddr1(config.getIpAddr1())
                .setIpAddr2(config.getIpAddr2())
                .setIpAddr3(config.getIpAddr3())
                .setSubnetMask0(config.getSubnetMask0())
                .setSubnetMask1(config.getSubnetMask1())
                .setSubnetMask2(config.getSubnetMask2())
                .setSubnetMask3(config.getSubnetMask3())
                .setDefaultGateway0(config.getDefaultGateway0())
                .setDefaultGateway1(config.getDefaultGateway1())
                .setDefaultGateway2(config.getDefaultGateway2())
                .setDefaultGateway3(config.getDefaultGateway3())
                .setSub1GWorkChannel(config.getSub1GWorkChannel())
                .setSub1GSweepSwitch(config.getSub1GSweepSwitch())
                .setCableDns0(config.getCableDns0())
                .setCableDns1(config.getCableDns1())
                .setCableDns2(config.getCableDns2())
                .setCableDns3(config.getCableDns3())
                ;
    }
}
