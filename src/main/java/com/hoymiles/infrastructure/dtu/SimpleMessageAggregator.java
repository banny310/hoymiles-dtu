package com.hoymiles.infrastructure.dtu;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageAggregationException;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public abstract class SimpleMessageAggregator<I> extends MessageToMessageDecoder<I> {

    private I aggregated;
    private ChannelFutureListener continueResponseWriteListener;

    protected abstract boolean isStartMessage(I msg) throws Exception;

    protected abstract boolean isContentMessage(I msg) throws Exception;

    protected abstract boolean isLastContentMessage(I msg) throws Exception;

    protected abstract I aggregate(I start, I content) throws Exception;

    protected abstract Object continueResponse(I current, ChannelPipeline pipeline) throws Exception;

    @Override
    protected void decode(ChannelHandlerContext ctx, I msg, List<Object> out) throws Exception {
        if (isStartMessage(msg)) {
            log.info("First message");
            if (aggregated != null) {
                throw new MessageAggregationException("Message number mismatch");
            }
            aggregated = msg;

            replyToContinue(ctx, msg);

        } else if(isContentMessage(msg)) {
            log.info("Next message, last=" + isLastContentMessage(msg));
            if (aggregated == null) {
                throw new MessageAggregationException("Message number mismatch");
            }

            aggregated = aggregate(aggregated, msg);

            if (isLastContentMessage(msg)) {
                out.add(aggregated);
                aggregated = null;
            } else {
                replyToContinue(ctx, msg);
            }
        } else {
            // pass through
            out.add(msg);
        }
    }

    private void replyToContinue(ChannelHandlerContext ctx, I current) throws Exception {
        Object continueResponse = continueResponse(current, ctx.pipeline());
        if (continueResponse != null) {
            ChannelFutureListener listener = continueResponseWriteListener;
            if (listener == null) {
                continueResponseWriteListener = listener = future -> {
                    if (!future.isSuccess()) {
                        ctx.fireExceptionCaught(future.cause());
                    }
                };
            }

            ctx.writeAndFlush(continueResponse).addListener(listener);
        }
    }
}
