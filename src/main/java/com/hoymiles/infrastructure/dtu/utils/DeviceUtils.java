package com.hoymiles.infrastructure.dtu.utils;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;


public class DeviceUtils {
    private static final Logger logger = LogManager.getLogger(DeviceUtils.class);

    public static void linePbObj(GeneratedMessageV3 generatedMessageV3) {
        logger.debug(generatedMessageV3.toString().replaceAll("\t|\r|\n", ","));
        logger.debug("------------------------------------------------------------------");
        logger.debug(MyJsonFormat.printToString(generatedMessageV3));
        logger.debug("▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲");
    }

    public static ByteString toByteString(String str) {
        return ByteString.copyFrom(str, CharsetUtil.ISO_8859_1);
    }

    public static int getCurrentTime() {
        return getUnsignedInt(System.currentTimeMillis() / 1000);
    }

    public static int getUnsignedInt(long j) {
        byte[] bArr = new byte[4];
        ByteBuffer.wrap(bArr).putInt((int) j);
        return ByteBuffer.wrap(bArr).getInt();
    }

    public static String decToHex(String str) {
        StringBuffer stringBuffer = new StringBuffer();
        char[] cArr = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        for (Long valueOf = Long.valueOf(Long.parseLong(str)); valueOf.longValue() != 0; valueOf = Long.valueOf(valueOf.longValue() / 16)) {
            stringBuffer.append(cArr[(int) (valueOf.longValue() % 16)]);
        }
        return stringBuffer.reverse().toString();
    }
}
