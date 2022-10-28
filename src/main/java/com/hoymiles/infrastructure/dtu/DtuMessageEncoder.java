package com.hoymiles.infrastructure.dtu;

import com.google.protobuf.Message;
import com.hoymiles.infrastructure.dtu.utils.CRC16Util;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DtuMessageEncoder extends MessageToByteEncoder<DtuMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, DtuMessage dtuMessage, ByteBuf out) throws Exception {
        Message msg = dtuMessage.getProto();
        int code = dtuMessage.getCode();
        ByteBuf buffer = Unpooled.buffer();

        try {
            byte[] bArr = msg.toByteArray();
            buffer.writeBytes("HM".getBytes());
            buffer.writeShort(code);
            buffer.writeShort(1);
            buffer.writeShort(CRC16Util.crc16(bArr));
            buffer.writeShort(bArr.length + 10);
            buffer.writeBytes(bArr);
            byte[] bArr2 = new byte[buffer.readableBytes()];
            buffer.getBytes(buffer.readerIndex(), bArr2);
            log.debug("--> dataPacket: " + ByteBufUtil.hexDump(buffer));
            log.debug("▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲");
            out.writeBytes(buffer);
        } finally {
            buffer.release();
        }
    }
}
