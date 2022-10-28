package com.hoymiles.infrastructure.dtu;

import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class DtuMessageDecoder extends ByteToMessageDecoder {
    private final int HM_HEADER_SIZE = 10;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, @NotNull ByteBuf in, @NotNull List<Object> list) throws Exception {
        int len = in.readableBytes();
        log.info("<-- decode: incoming message, length={}", len);

        try {
            if (in.readableBytes() < HM_HEADER_SIZE) {
                // return to allow more bytes to arrive
                in.resetReaderIndex();
                String s = String.format("Message incomplete: got %d readable bytes", in.readableBytes());
                throw new DecoderException("");
            }

            String hexDump = ByteBufUtil.hexDump(in);
            log.info(hexDump.substring(0, 20) + "|" + hexDump.substring(20));

            final String hmHeader = in.readCharSequence(2, StandardCharsets.ISO_8859_1).toString();
            final int msgId = in.readUnsignedShort();
            final short counter = in.readShort();      // ???
            final short crc = in.readShort();          // ??? crc
            final short msgLength = in.readShort();
            final int dataLength = msgLength - HM_HEADER_SIZE;

            log.info(String.format("header=%s, msgId=%d, counter=%d, crc=%d, msgLen=%d", hmHeader, msgId, counter, crc, msgLength));

            if (!hmHeader.equals("HM")) {
                String s = String.format("Malformed header prefix: got %s instead of HM", hmHeader);
                // discard buffer (mark buffer read)
                in.readerIndex(in.readerIndex() + in.readableBytes());
                throw new DecoderException(s);
            }
            if (in.readableBytes() < dataLength) {
                // return to allow more bytes to arrive
                in.resetReaderIndex();
                String s = String.format("Malformed message size: got %d readable bytes, but message is %d bytes", in.readableBytes(), msgLength);
                throw new DecoderException(s);
            }

            final byte[] bArr = new byte[dataLength];
            in.readBytes(bArr, 0, dataLength);

            DtuMessageHandler handler = DtuMessageHandler.findHandler(msgId);
            Message msg = handler.fromByte(bArr);
            list.add(new DtuMessage(msgId, msg));

            log.info(msg.toString().replaceAll("[\t\r\n]", ", "));
        } catch (DecoderException | NoHandlerException e) {
            log.warn("DecoderException: " + e.getMessage());
        } finally {
            log.info("<-- end");
        }
    }
}
