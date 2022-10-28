package com.hoymiles.infrastructure.dtu;

import com.google.protobuf.Message;
import com.hoymiles.infrastructure.dtu.utils.CRC16Util;
import com.hoymiles.infrastructure.protos.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Arrays;

@Getter
public class DtuMessageHandler {
    private static final Logger logger = LogManager.getLogger(DtuMessageHandler.class);

    static DtuMessageHandler[] map = {
            // CommandPB.proto
            new DtuMessageHandler(DtuClientConstants.TAG_AppConfigCommandRes, CommandPB.CommandResDTO.class),
            new DtuMessageHandler(DtuClientConstants.TAG_AppConfigCommandReq, CommandPB.CommandReqDTO.class),
            // APPInformationData.proto
            new DtuMessageHandler(DtuClientConstants.TAG_AppInfoRes, APPInformationData.APPInfoDataResDTO.class),
            new DtuMessageHandler(DtuClientConstants.TAG_AppInfoReq, APPInformationData.APPInfoDataReqDTO.class),
            // RealData.proto
            new DtuMessageHandler(DtuClientConstants.TAG_AppRealDataRes, RealData.RealDataResDTO.class),
            new DtuMessageHandler(DtuClientConstants.TAG_AppRealDataReq, RealData.RealDataReqDTO.class),
            // RealDataNew.proto
            // new protocol (dtuSw >= 512)
            new DtuMessageHandler(DtuClientConstants.TAG_AppRealDataRes_X, RealDataNew.RealResDTO.class),
            new DtuMessageHandler(DtuClientConstants.TAG_AppRealDataReq_X, RealDataNew.RealReqDTO.class),
            new DtuMessageHandler(8716, RealDataNew.RealReqDTO.class),
            new DtuMessageHandler(8717, RealDataNew.RealReqDTO.class),
            new DtuMessageHandler(8715, Unknown.Msg8715.class),
            // GetConfig.proto
            new DtuMessageHandler(DtuClientConstants.TAG_AppGetConfigRes, GetConfig.GetConfigRes.class),
            new DtuMessageHandler(DtuClientConstants.TAG_AppGetConfigReq, GetConfig.GetConfigReq.class),
            // SetConfig.proto
            new DtuMessageHandler(DtuClientConstants.TAG_AppSetConfigRes, SetConfig.SetConfigRes.class),
            new DtuMessageHandler(DtuClientConstants.TAG_AppSetConfigReq, SetConfig.SetConfigReq.class),

            new DtuMessageHandler(8971, GenericCommand.GenericCommandResDTO2.class),
            new DtuMessageHandler(8972, GenericCommand.GenericCommandResDTO2.class),
            new DtuMessageHandler(8962, GenericCommand.GenericCommandResDTO2.class),

            new DtuMessageHandler(8705, Unknown.Msg8705.class),
            new DtuMessageHandler(8706, Unknown.Msg8706.class),
    };

    private final int code;
    private final Class<? extends Message> clazz;

    private DtuMessageHandler(int code, Class<? extends Message> clazz) {
        this.code = code;
        this.clazz = clazz;
    }

    public Message fromByte(byte[] data) throws Exception {
        Method parse = clazz.getMethod("parseFrom", byte[].class);
        return (Message) parse.invoke(null, data);
    }

    public byte[] toByte(@NotNull Message msg) {
        byte[] bArr = msg.toByteArray();
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeBytes("HM".getBytes());
        buffer.writeShort(code);
        buffer.writeShort(1);
        buffer.writeShort(CRC16Util.crc16(bArr));
        buffer.writeShort(bArr.length + 10);
        buffer.writeBytes(bArr);
        byte[] bArr2 = new byte[buffer.readableBytes()];
        buffer.getBytes(buffer.readerIndex(), bArr2);
        logger.debug("--> dataPacket: " + ByteBufUtil.hexDump(buffer));
        logger.debug("▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲");
        buffer.release();
        return bArr2;
    }

    public static @NotNull DtuMessageHandler findHandler(Class<? extends Message> clazz) throws NoHandlerException {
        return Arrays.stream(map)
                .filter(messageHandler -> messageHandler.clazz == clazz)
                .findFirst()
                .orElseThrow(() -> new NoHandlerException("No handler found for class " + clazz.getName()));
    }

    public static @NotNull DtuMessageHandler findHandler(int code) throws NoHandlerException {
        return Arrays.stream(map)
                .filter(messageHandler -> messageHandler.code == code)
                .findFirst()
                .orElseThrow(() -> new NoHandlerException("No handler found for code " + code));
    }
}
