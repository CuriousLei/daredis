package cn.buptleida.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class FixedLengthFrameEncoder extends MessageToByteEncoder<String> {
    private int length;

    public FixedLengthFrameEncoder(int length) {
        this.length = length;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out)
            throws Exception {
        // 对于超过指定长度的消息，这里直接抛出异常
        if (msg.length() > length) {
            throw new UnsupportedOperationException(
                    "message length is too large, it's limited " + length);
        }

        // 如果长度不足，则进行补全
        if (msg.length() < length) {
            msg = addSpace(msg);
        }

        ctx.writeAndFlush(Unpooled.wrappedBuffer(msg.getBytes()));
    }

    // 进行空格补全
    private String addSpace(String msg) {
        StringBuilder builder = new StringBuilder(msg);
        for (int i = 0; i < length - msg.length(); i++) {
            builder.append(" ");
        }

        return builder.toString();
    }
}
