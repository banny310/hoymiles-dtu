package com.hoymiles.infrastructure.dtu;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import com.hoymiles.infrastructure.dtu.utils.DeviceUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.spi.BeanManager;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@Log4j2
public class DtuClient {
    private final BeanManager beanManager;
    private final Bootstrap bootstrap;
    private final EventLoopGroup group;
    private final List<Pair<Integer, ObservableEmitter<? extends Message>>> emitters = new ArrayList<>();

    private ChannelFuture connectionFuture;
    private String host;
    private int port;

    public DtuClient(BeanManager beanManager) {
        this.beanManager = beanManager;

        group = new NioEventLoopGroup(1);
        bootstrap = new Bootstrap();
        bootstrap.group(group);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            public void initChannel(SocketChannel channel) {
                channel.pipeline().addLast(new IdleStateHandler(10L, 10L, 10L, TimeUnit.SECONDS));
                channel.pipeline().addLast(new InboundDecoder(), new InboundHandler());
            }
        });
    }

    public void connect(String host, int port) throws InterruptedException {
        this.host = host;
        this.port = port;
        log.info(String.format("Trying connect to %s:%d", host, port));
        connectionFuture = bootstrap.connect(host, port).sync();
        if (connectionFuture.isSuccess()) {
            log.info(String.format("Connect success to %s:%d", host, port));
        } else {
            log.warn("Connect error!");
        }
    }

    private void reconnect() throws InterruptedException {
        connect(host, port);
    }

    public void close() {
        if (connectionFuture != null) {
            connectionFuture.channel().closeFuture().awaitUninterruptibly();
            group.shutdownGracefully();
        }
    }

    public void send(Message message) throws NoHandlerException {
        if (!connectionFuture.isSuccess()) {
            throw new IllegalStateException("Not connected");
        }

        try {
            DtuMessageHandler messageHandler = DtuMessageHandler.findHandler(message.getClass());
            log.info("--> sending: msgId={}", messageHandler.getCode());
            log.info(message.toString().replaceAll("\t|\r|\n", ", "));
            ByteBuf buffer = Unpooled.buffer();
            buffer.writeBytes(messageHandler.toByte(message));
            connectionFuture.channel().writeAndFlush(buffer);
        } catch (NoHandlerException e) {
            log.info("--> sending: msgId={}", "unknown");
            log.info(message.toString().replaceAll("\t|\r|\n", ", "));
            throw e;
        } finally {
            log.info("--> end");
        }
    }

    public <T extends Message> Observable<T> command(Message message, Class<T> responseClazz) {
        return Observable.create(emitter -> {
            DtuMessageHandler responseHandler = DtuMessageHandler.findHandler(responseClazz);
            emitters.add(new ImmutablePair<>(responseHandler.getCode(), emitter));
            send(message);
        });
    }

    public static class InboundDecoder extends ByteToMessageDecoder {
        @Override
        protected void decode(ChannelHandlerContext channelHandlerContext, @NotNull ByteBuf byteBuf, @NotNull List<Object> list) throws Exception {
            int len = byteBuf.readableBytes();
            log.info("<-- decode: incoming message, length={}", len);

            String hexDump = ByteBufUtil.hexDump(byteBuf);
            log.info(hexDump.substring(0, 20) + "|" + hexDump.substring(20));

            try {
                assert byteBuf.readableBytes() >= 10;

                byteBuf.resetReaderIndex();
                String hmHeader = byteBuf.readCharSequence(2, StandardCharsets.ISO_8859_1).toString();
                final int msgId = byteBuf.readUnsignedShort();
                final short counter = byteBuf.readShort();      // ???
                final short crc = byteBuf.readShort();          // ??? crc
                final short msgLen = byteBuf.readShort();
                final int dataLength = msgLen - 10;

                assert hmHeader.equals("HM");
                assert msgLen * 2 == hexDump.length();
                assert dataLength > 0;
                assert dataLength <= len;

                log.info(String.format("header=%s, msgId=%d, counter=%d, crc=%d, msgLen=%d", hmHeader, msgId, counter, crc, msgLen));

                final byte[] bArr = new byte[dataLength];
                byteBuf.readBytes(bArr, 0, dataLength);

                DtuMessageHandler handler = DtuMessageHandler.findHandler(msgId);
                Message msg = handler.fromByte(bArr);
                list.add(new DtuMessage(msgId, msg));

                log.info(msg.toString().replaceAll("\t|\r|\n", ", "));
            } catch (NoHandlerException e) {
                log.warn(e.getMessage());
            } catch (Throwable e) {
                log.error(e);
            } finally {
                log.info("<-- end");
            }
        }
    }

    public class InboundHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            super.channelRead(ctx, msg);
            try {
                if (msg instanceof DtuMessage) {
                    DtuMessage dtuMsg = (DtuMessage) msg;
                    DeviceUtils.linePbObj((GeneratedMessageV3) dtuMsg.getMessage());

                    beanManager.fireEvent(dtuMsg);

                    Predicate<Pair<Integer, ObservableEmitter<? extends Message>>> isQualified = item -> item.getKey() == dtuMsg.getCode();
                    emitters.stream()
                            .filter(isQualified)
                            .forEach(el -> ((ObservableEmitter<Message>) el.getValue()).onNext(dtuMsg.getMessage()));
                    emitters.removeIf(isQualified);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        @SneakyThrows
        public void exceptionCaught(@NotNull ChannelHandlerContext ctx, Throwable cause) {
            ctx.close();
            log.warn("exceptionCaught: " + cause.getMessage());
            if (cause instanceof IOException) {
                reconnect();
            }
        }
    }
}
