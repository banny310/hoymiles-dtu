package com.hoymiles.infrastructure.dtu;

import com.google.protobuf.Message;
import com.hoymiles.infrastructure.dtu.utils.CRC16Util;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

@Slf4j
public class DtuMessageDecoder extends ByteToMessageDecoder {
    private final int HM_HEADER_SIZE = 10;
    private final FixedSizeList<Long> errorFrames = new FixedSizeList<>(new ArrayList<>(), 10);

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, @NotNull ByteBuf in, @NotNull List<Object> list) throws Exception {
        log.info("<-- decode: incoming message, length={}", in.readableBytes());

        // calc control sum of entire buffer
        final byte[] crcArr = new byte[in.readableBytes()];
        in.getBytes(0, crcArr);
        CRC32 crc32 = new CRC32();
        crc32.update(crcArr);
        final long crc = crc32.getValue();

        try {
            if (in.readableBytes() < 2) {
                // return to allow more bytes to arrive
                return;
            }

            int pos = scanForNextHM(in);
            if (pos == -1) {
                // no HM header in buffer
                // return to allow more bytes to arrive
                return;
            }
            if (pos > 0) {
                // skip bytes until next correct message
                log.warn("No correct header at a beginning. Skipping {} bytes", pos);
                in.skipBytes(pos);
            }

            if (in.readableBytes() < HM_HEADER_SIZE) {
                // rest of buffer too small to contain message
                // return to allow more bytes to arrive
                return;
            }

            String hexDump = ByteBufUtil.hexDump(in);
            log.info(hexDump.substring(0, 20) + "|" + hexDump.substring(20));

            final String hmHeader = in.getCharSequence(0,2, StandardCharsets.ISO_8859_1).toString();
            final int msgId = in.getUnsignedShort(2);
            final int msgCounter = in.getUnsignedShort(4);
            final int dataCrc = in.getUnsignedShort(6);
            final int msgLength = in.getUnsignedShort(8);
            final int dataLength = msgLength - HM_HEADER_SIZE;

            log.info("header={}, msgId={}, counter={}, dataCrc={}, msgLen={}", hmHeader, msgId, msgCounter, dataCrc, msgLength);

            if (in.readableBytes() < msgLength) {
                if (errorFrames.contains(crc)) {
                    // second time same error frame - discard
                    log.warn("Malformed message size: second time got {} readable bytes, but message is {} bytes - discarding", in.readableBytes(), msgLength);
                    in.skipBytes(in.readableBytes());
                    return;
                }

                errorFrames.add(crc);
                log.warn("Malformed message size: got {} readable bytes, but message is {} bytes", in.readableBytes(), msgLength);
                // return to allow more bytes to arrive
                return;
            }

            in.skipBytes(10);
            final byte[] data = new byte[dataLength];
            in.readBytes(data, 0, dataLength);

            int calcCrc = CRC16Util.crcModRTU(data, dataLength);
            if (dataCrc != calcCrc) {
                log.warn("Malformed message crc: got {} instead of {} - discarding", calcCrc, dataCrc);
                return;
            }

            DtuMessageHandler handler = DtuMessageHandler.findHandler(msgId);
            Message msg = handler.fromByte(data);
            list.add(new DtuMessage(msgId, msg));

            log.info(msg.toString().replaceAll("[\t\r\n]", ", "));
        } catch (NoHandlerException e) {
            log.warn("DecoderException: " + e.getMessage());
        } finally {
            log.info("<-- end");
        }
    }

    private int scanForNextHM(ByteBuf in) {
        int pos = -1;
        while (pos <= in.readableBytes() - 2) {
            String h = in.getCharSequence(++pos, 2, StandardCharsets.ISO_8859_1).toString();
            if (h.equals("HM")) {
                return pos;
            }
        }

        return -1;
    }
}
