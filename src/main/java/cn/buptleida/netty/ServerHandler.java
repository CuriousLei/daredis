package cn.buptleida.netty;

import cn.buptleida.database.RedisClient;
import cn.buptleida.database.RedisServer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class ServerHandler extends ChannelHandlerAdapter {

    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private RedisClient client;

    public ServerHandler() {
        this.client = new RedisClient();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        //do something msg
        // ByteBuf buf = (ByteBuf)msg;
        // byte[] data = new byte[buf.readableBytes()];
        // buf.readBytes(data);
        // String request = new String(data, "utf-8");
        String request = (String)msg;
        //System.out.println("接收到" + request.trim());
        String returnMsg = RedisServer.commandExecute(client, request.trim());
        System.out.println(returnMsg);
        //String returnMsg = "+OK";
        //ctx.writeAndFlush(Unpooled.copiedBuffer(returnMsg.getBytes()));
        ctx.writeAndFlush(returnMsg);
    }

    // @Override
    // public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    //     cause.printStackTrace();
    //     ctx.close();
    // }

    /**
     * 每当服务端收到新的客户端连接时,客户端的channel存入ChannelGroup列表中,并通知列表中其他客户端channel
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        //获取连接的channel
        channels.add(ctx.channel());
    }

    /**
     *每当服务端断开客户端连接时,客户端的channel从ChannelGroup中移除,并通知列表中其他客户端channel
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        //获取连接的channel
        Channel incomming = ctx.channel();
        for(Channel channel:channels){
            channel.writeAndFlush("[SERVER]-"+incomming.remoteAddress()+"离开\n");
        }
        //从服务端的channelGroup中移除当前离开的客户端
        channels.remove(ctx.channel());
    }


    /**
     * 服务端监听到客户端活动
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //服务端接收到客户端上线通知
        Channel incoming = ctx.channel();
        System.out.println("SimpleChatClient:" + incoming.remoteAddress()+"在线");
    }

    /**
     * 服务端监听到客户端不活动
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //服务端接收到客户端掉线通知
        Channel incoming = ctx.channel();
        System.out.println("SimpleChatClient:" + incoming.remoteAddress()+"掉线");
    }

    /**
     * 当服务端的IO 抛出异常时被调用
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //super.exceptionCaught(ctx, cause);
        Channel incoming = ctx.channel();
        System.out.println("SimpleChatClient:" + incoming.remoteAddress()+"异常");
        //异常出现就关闭连接
        cause.printStackTrace();
        ctx.close();
    }


}
