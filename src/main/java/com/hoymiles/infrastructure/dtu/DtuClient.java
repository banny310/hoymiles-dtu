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
import io.reactivex.rxjava3.disposables.Disposable;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@ApplicationScoped
@Log4j2
public class DtuClient {
    public interface Listener {
        void onEvent(DtuMessage dtuMessage);

        void onError(Throwable throwable);

        void onConnectionLost(Throwable cause);
    }
    private final EventLoopGroup group;
    private final List<Pair<Integer, ObservableEmitter<? extends Message>>> emitters = new ArrayList<>();
    private Channel channel;
    private Listener listener;
    private Disposable disposable;
    private int watchdogTimeout;

    public DtuClient() {
        group = new NioEventLoopGroup(1);
    }

    public boolean isConnected() {
        return Optional.ofNullable(channel)
                .map(Channel::isActive)
                .orElse(false);
    }

    public void connect(String host, int port, int watchdogTimeout) throws InterruptedException {
        this.watchdogTimeout = watchdogTimeout;

        Bootstrap bootstrap = new Bootstrap();
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

        log.info(String.format("Trying connect to %s:%d", host, port));

        ChannelFuture channelFuture = bootstrap.connect(host, port).sync();

        channel = channelFuture.channel();
        channel.closeFuture().addListener(ChannelFutureListener.CLOSE);
        channel.closeFuture().addListener(f -> listener.onConnectionLost(f.cause()));

        if (channelFuture.isSuccess()) {
            log.info(String.format("Connect success to %s:%d", host, port));
        } else {
            log.warn("Connect error!");
        }
    }

    public void disconnect() {
        if (isConnected()) {
            log.info("Disconnecting from DTU...");
            ChannelFuture closeFuture = channel.disconnect();
            closeFuture.awaitUninterruptibly();
            group.shutdownGracefully(2, 5, TimeUnit.SECONDS);
        }
    }

    public void send(Message message) throws NoHandlerException {
        if (!isConnected()) {
            throw new DtuNotConnectedException();
        }

        try {
            DtuMessageHandler messageHandler = DtuMessageHandler.findHandler(message.getClass());
            log.info("--> sending: msgId={}", messageHandler.getCode());
            log.info(message.toString().replaceAll("[\t\r\n]", ", "));
            ByteBuf buffer = Unpooled.buffer();
            buffer.writeBytes(messageHandler.toByte(message));
            channel.writeAndFlush(buffer);
        } catch (NoHandlerException e) {
            log.info("--> sending: msgId={}", "unknown");
            log.info(message.toString().replaceAll("[\t\r\n]", ", "));
            throw e;
        } finally {
            log.info("--> end");
        }
    }

    public <T extends Message> Observable<T> command(Message message, Class<T> responseClazz) {
        return Observable.create(emitter -> {
            DtuMessageHandler responseHandler = DtuMessageHandler.findHandler(responseClazz);
            emitters.add(new ImmutablePair<>(responseHandler.getCode(), emitter));
            log.info("Sending command: message={}, responseClazz={}", message.getClass().getName(), responseClazz.getName());
            send(message);
        });
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public static class InboundDecoder extends ByteToMessageDecoder {
        @Override
        protected void decode(ChannelHandlerContext channelHandlerContext, @NotNull ByteBuf byteBuf, @NotNull List<Object> list) {
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

                log.info(msg.toString().replaceAll("[\t\r\n]", ", "));
            } catch (NoHandlerException e) {
                log.warn(e.getMessage());
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            } finally {
                log.info("<-- end");
            }
        }
    }

    public class InboundHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            super.channelRead(ctx, msg);

            if (msg instanceof DtuMessage) {
                DtuMessage dtuMsg = (DtuMessage) msg;
                DeviceUtils.linePbObj((GeneratedMessageV3) dtuMsg.getMessage());

                // find consumer for command
                Predicate<Pair<Integer, ObservableEmitter<? extends Message>>> isQualified = item -> item.getKey() == dtuMsg.getCode();
                //noinspection unchecked
                emitters.stream()
                        .filter(isQualified)
                        .forEach(el -> ((ObservableEmitter<Message>) el.getValue()).onNext(dtuMsg.getMessage()));
                boolean consumed = emitters.removeIf(isQualified);

                // it is event if not consumed
                if (!consumed) {
                    listener.onEvent(dtuMsg);
                }

                // start watchdog after each packet receive
                if (watchdogTimeout > 0) {
                    Optional.ofNullable(disposable).ifPresent(Disposable::dispose);
                    disposable = Observable.just("Watchdog timeout")
                            .delay(watchdogTimeout, TimeUnit.SECONDS)
                            .subscribe(s -> {
                                log.warn(s);
                                ChannelFuture closeFuture = channel.disconnect();
                                closeFuture.awaitUninterruptibly();
                            });
                }

//                if (((DtuMessage) msg).getCode() == 8706) {
//                    throw new IOException("Reconnection test");
//                }
            }
        }

        @Override
        @SneakyThrows
        public void exceptionCaught(@NotNull ChannelHandlerContext ctx, Throwable cause) {
            log.error("exceptionCaught: " + cause.getMessage());
            listener.onError(cause);
            ctx.close().awaitUninterruptibly(5000L);
        }
    }
}
