package com.hoymiles.infrastructure.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

@Dependent
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class TcpServer {

    @SneakyThrows(InterruptedException.class)
    public void start(int port) {
        EventLoopGroup group = new NioEventLoopGroup();
        // 10.10.100.254
        log.info("Starting server at: " + port);
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();// (2)
        try {
            ServerBootstrap b = new ServerBootstrap();// (3)
            b.group(bossGroup, workerGroup) // (4)
                    .channel(NioServerSocketChannel.class)// (5)
                    .childHandler(new SimpleTCPChannelInitializer())// (6)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);// (7)

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync();// (8)
            if (f.isSuccess()) log.info("Server started successfully"); // (9)
            f.channel().closeFuture().sync(); // (10)
        } finally {
            log.info("Stopping server");
            workerGroup.shutdownGracefully();// (11)
            bossGroup.shutdownGracefully();// (12)
        }
    }

    public class SimpleTCPChannelInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
//            socketChannel.pipeline().addLast(new StringEncoder()); // (1)
//            socketChannel.pipeline().addLast(new StringDecoder());// (2)
            socketChannel.pipeline().addLast(new HelloServerHandler());//(3)
        }
    }

    public class SimpleTCPChannelHandler extends SimpleChannelInboundHandler<String> {

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            log.info("Channel Active: " + ctx.channel().remoteAddress());
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
            log.info(s + ctx.channel().remoteAddress());// (1)
            //ctx.channel().writeAndFlush("Thanks\n"); // (2)
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            log.info("Channel Inactive" + ctx.channel().remoteAddress());
        }
    }

    public class HelloServerHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            log.info("Channel Active: " + ctx.channel().remoteAddress());
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf byteBuf = (ByteBuf) msg;
            String received = byteBuf.toString(CharsetUtil.UTF_8);
            log.info("Server received: " + received);

            byteBuf.resetReaderIndex();
            String hexDump = ByteBufUtil.hexDump(byteBuf);
            log.info(hexDump.substring(0, 20) + "|" + hexDump.substring(20));

            byteBuf.resetReaderIndex();
            String hmHeader = byteBuf.readCharSequence(2, StandardCharsets.ISO_8859_1).toString();
            final int msgId = byteBuf.readUnsignedShort();
            final short counter = byteBuf.readShort();      // ???
            final short crc = byteBuf.readShort();          // ??? crc
            final short msgLen = byteBuf.readShort();
            final int dataLength = msgLen - 10;

            log.info(String.format("header=%s, msgId=%d, counter=%d, crc=%d, msgLen=%d", hmHeader, msgId, counter, crc, msgLen));

            ctx.write(Unpooled.copiedBuffer("Hello " + received, CharsetUtil.UTF_8));
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
                    .addListener(ChannelFutureListener.CLOSE);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            log.info("Channel Inactive" + ctx.channel().remoteAddress());
        }
    }
}
