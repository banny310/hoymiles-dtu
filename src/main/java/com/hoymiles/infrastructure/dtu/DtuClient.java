package com.hoymiles.infrastructure.dtu;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import com.hoymiles.infrastructure.dtu.utils.DeviceUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.MessageAggregationException;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
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

    private final DtuCommandBuilder dtuCommand;
    private final EventLoopGroup group;
    private final List<Pair<Integer, ObservableEmitter<? extends Message>>> emitters = new ArrayList<>();
    private Channel channel;
    private Listener listener;

    @Inject
    public DtuClient(DtuCommandBuilder dtuCommandBuilder) {
        dtuCommand = dtuCommandBuilder;
        group = new NioEventLoopGroup(1);
    }

    public boolean isConnected() {
        return Optional.ofNullable(channel)
                .map(Channel::isActive)
                .orElse(false);
    }

    public void connect(String host, int port, int watchdogTimeout) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            public void initChannel(SocketChannel channel) {
                channel.pipeline().addFirst("encoder", new DtuMessageEncoder());
                channel.pipeline().addLast("watchdog", new IdleStateHandler(watchdogTimeout, 10L, 0L, TimeUnit.SECONDS));
                channel.pipeline().addLast("decoder", new DtuMessageDecoder());
                channel.pipeline().addLast("aggregator", new DtuMessageAggregator(dtuCommand));
                channel.pipeline().addLast("response", new InboundHandler());
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

    public void send(Message message) {
        if (!isConnected()) {
            throw new DtuNotConnectedException();
        }

        try {
            DtuMessageHandler messageHandler = DtuMessageHandler.findHandler(message.getClass());
            send(new DtuMessage(messageHandler.getCode(), message));
        } catch (NoHandlerException e) {
            log.info("--> sending: msgId={}", "unknown");
            log.info(message.toString().replaceAll("[\t\r\n]", ", "));
            throw new RuntimeException(e);
        } finally {
            log.info("--> end");
        }
    }

    public void send(DtuMessage message) {
        log.info("--> sending: msgId={}", message.getCode());
        log.info(message.getProto().toString().replaceAll("[\t\r\n]", ", "));
        channel.writeAndFlush(message);
    }

    public <T extends Message> Observable<T> command(Message message, Class<T> responseClazz) {
        return Observable.create(emitter -> {
            synchronized (this) {
                DtuMessageHandler responseHandler = DtuMessageHandler.findHandler(responseClazz);
                emitters.add(new ImmutablePair<>(responseHandler.getCode(), emitter));
                log.info("Sending command: message={}, responseClazz={}", message.getClass().getName(), responseClazz.getName());
                send(message);
            }
        });
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @RequiredArgsConstructor
    public class InboundHandler extends SimpleChannelInboundHandler<DtuMessage> {
        @Override
        public void channelRead0(ChannelHandlerContext ctx, DtuMessage dtuMsg) {
            DeviceUtils.linePbObj((GeneratedMessageV3) dtuMsg.getProto());

            // find consumer for command
            synchronized (this) {
                Predicate<Pair<Integer, ObservableEmitter<? extends Message>>> isQualified = item -> item.getKey() == dtuMsg.getCode();
                //noinspection unchecked
                emitters.stream()
                        .filter(isQualified)
                        .forEach(el -> ((ObservableEmitter<Message>) el.getValue()).onNext(dtuMsg.getProto()));
                boolean consumed = emitters.removeIf(isQualified);

                // it is event if not consumed
                if (!consumed) {
                    listener.onEvent(dtuMsg);
                }
            }
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent e = (IdleStateEvent) evt;
                if (e.state() == IdleState.READER_IDLE) {
                    log.warn("Watchdog timeout");
                    ctx.close();
                }
            }
        }

        @Override
        @SneakyThrows
        public void exceptionCaught(@NotNull ChannelHandlerContext ctx, Throwable cause) {

            // java.io.IOException: Connection reset by peer
            if (cause instanceof IOException) {
                log.warn("IOException: " + cause.getMessage());
                // close connection, and allow to reconnect
                ctx.close();
            } else if (cause instanceof MessageAggregationException) {
                log.warn("MessageAggregationException: " + cause.getMessage());
            } else
            // forward all others to App
            {
                log.error("exceptionCaught: " + cause.getMessage(), cause);
                listener.onError(cause);
            }
        }
    }
}
