package cn.buptleida.nettyTest;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

public class ClientHandler extends ChannelHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            // ByteBuf buf = (ByteBuf) msg;
            // byte[] data = new byte[buf.readableBytes()];
            // buf.readBytes(data);
            // System.out.println("Client：" + new String(data).trim());
            //System.out.println("回执:" + msg);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
