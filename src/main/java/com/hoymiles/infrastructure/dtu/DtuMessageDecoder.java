package com.hoymiles.infrastructure.dtu;

import com.google.protobuf.Message;
import com.hoymiles.infrastructure.dtu.utils.CRC16Util;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.CRC32;

@Slf4j
public class DtuMessageDecoder extends ByteToMessageDecoder {
    private final int HM_HEADER_SIZE = 10;
    private long prevCrc = 0;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, @NotNull ByteBuf in, @NotNull List<Object> list) throws Exception {
        log.info("<-- decode: incoming message, length={}", in.readableBytes());

        // calc control cum of entire message
        final byte[] crcArr = new byte[in.readableBytes()];
        in.getBytes(0, crcArr);
        CRC32 crc32 = new CRC32();
        crc32.update(crcArr);
        final long crc = crc32.getValue();

        try {
            // validate message size header readable
            if (in.readableBytes() < HM_HEADER_SIZE) {
                String s = String.format("Message incomplete: got %d readable bytes", in.readableBytes());

                if (prevCrc == crc) {
                    // same message second time
                    // discard buffer
                    in.skipBytes(in.readableBytes());
                    prevCrc = 0;
                    throw new DecoderException(s + " - discarding");
                }

                // return to allow more bytes to arrive
                in.resetReaderIndex();
                prevCrc = crc;
                throw new DecoderException(s);
            }

            String hexDump = ByteBufUtil.hexDump(in);
            log.info(hexDump.substring(0, 20) + "|" + hexDump.substring(20));

            final String hmHeader = in.readCharSequence(2, StandardCharsets.ISO_8859_1).toString();
            final int msgId = in.readUnsignedShort();
            final int msgCounter = in.readUnsignedShort();
            final int dataCrc = in.readUnsignedShort();
            final int msgLength = in.readUnsignedShort();
            final int dataLength = msgLength - HM_HEADER_SIZE;

            log.info("header={}, msgId={}, counter={}, dataCrc={}, msgLen={}", hmHeader, msgId, msgCounter, dataCrc, msgLength);

            if (!hmHeader.equals("HM")) {
                // discard buffer
                in.skipBytes(in.readableBytes());
                String s = String.format("Malformed header prefix: got %s instead of HM", hmHeader);
                throw new DecoderException(s);
            }

            if (in.readableBytes() < dataLength) {
                String s = String.format("Malformed message size: got %d readable bytes, but message is %d bytes", in.readableBytes(), msgLength);

                if (prevCrc == crc) {
                    // same message second time
                    // discard buffer
                    in.skipBytes(in.readableBytes());
                    prevCrc = 0;
                    throw new DecoderException(s + " - discarding");
                }

                // return to allow more bytes to arrive
                in.resetReaderIndex();
                prevCrc = crc;
                throw new DecoderException(s);
            }

            // reset crc on each correct message
            prevCrc = 0;

            final byte[] data = new byte[dataLength];
            in.readBytes(data, 0, dataLength);

            int calcCrc = CRC16Util.crcModRTU(data, dataLength);
            if (dataCrc != calcCrc) {
                String s = String.format("Malformed message crc: got %d instead of %d - discarding", calcCrc, dataCrc);
                throw new DecoderException(s);
            }

            DtuMessageHandler handler = DtuMessageHandler.findHandler(msgId);
            Message msg = handler.fromByte(data);
            list.add(new DtuMessage(msgId, msg));


            log.info(msg.toString().replaceAll("[\t\r\n]", ", "));

        } catch (DecoderException | NoHandlerException e) {
            log.warn("DecoderException: " + e.getMessage());
        } finally {
            log.info("<-- end");
        }
    }
}
