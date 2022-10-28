package com.hoymiles.infrastructure.dtu;

import com.google.protobuf.Message;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageAggregationException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@RequiredArgsConstructor
public class DtuMessageAggregator extends SimpleMessageAggregator<DtuMessage> {

    private final DtuCommandBuilder dtuCommand;

    @Override
    protected boolean isStartMessage(DtuMessage msg) {
        ContinuableMessageWrapper cmw = new ContinuableMessageWrapper(msg.getProto());
        return cmw.getPacketNum() == 0 && cmw.getPacketCount() > 1;
    }

    @Override
    protected boolean isContentMessage(DtuMessage msg) {
        ContinuableMessageWrapper cmw = new ContinuableMessageWrapper(msg.getProto());
        return cmw.getPacketNum() <= cmw.getPacketCount() - 1;
    }

    @Override
    protected boolean isLastContentMessage(DtuMessage msg) {
        ContinuableMessageWrapper cmw = new ContinuableMessageWrapper(msg.getProto());
        return cmw.getPacketNum() == cmw.getPacketCount() - 1;
    }

    @Override
    protected DtuMessage aggregate(DtuMessage start, DtuMessage content) {
        ContinuableMessageWrapper cmw = new ContinuableMessageWrapper(start.getProto());
        Message.Builder builder = cmw.getBuilder();
        if (builder != null) {
            Message aggregated = builder
                    .mergeFrom(start.getProto())
                    .mergeFrom(content.getProto())
                    .build();
            return new DtuMessage(start.getCode(), aggregated);
        }

        throw new MessageAggregationException("Cannot aggregate message of " + content.getCode());
    }

    @Override
    protected Object continueResponse(DtuMessage content, ChannelPipeline pipeline) {
        switch (content.getCode()) {
            case DtuClientConstants.TAG_AppRealDataReq_X:
                ContinuableMessageWrapper cmw = new ContinuableMessageWrapper(content.getProto());
                Message nextMessage = dtuCommand.realDataXBuilder().setCp(cmw.getPacketNum() + 1).build();
                return new DtuMessage(DtuClientConstants.TAG_AppRealDataRes_X, nextMessage);
            case 8716:
            case 8717:
                return null;
        }
        return null;
    }

    private static class ContinuableMessageWrapper {
        Message message;
        Class<?> clazz;

        ContinuableMessageWrapper(Message msg) {
            message = msg;
            clazz = message.getClass();
        }

        private int getPacketNum()  {
            try {
                Method m = clazz.getDeclaredMethod("getPacketNum");
                return (int) m.invoke(message);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {}

            return -1;
        }

        private int getPacketCount()  {
            try {
                Method m = clazz.getDeclaredMethod("getPacketCount");
                return (int) m.invoke(message);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {}

            return -1;
        }

        private @Nullable Message.Builder getBuilder()  {
            try {
                Method m = clazz.getDeclaredMethod("newBuilder");
                return (Message.Builder) m.invoke(null);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {}

            return null;
        }
    }
}
