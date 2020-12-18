package cn.buptleida.nio.core;

import cn.buptleida.nio.box.StringReceivePacket;
import cn.buptleida.nio.box.StringSendPacket;
import cn.buptleida.nio.impl.SocketChannelAdapter;
import cn.buptleida.nio.impl.async.AsyncReceiveDispatcher;
import cn.buptleida.nio.impl.async.AsyncSendDispatcher;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public class Connector implements Closeable, SocketChannelAdapter.OnChannelStatusChangedListener {

    private SocketChannel channel;
    private Sender sender;//这两个都引用适配器
    private Receiver receiver;
    private SendDispatcher sendDispatcher;
    private ReceiveDispatcher receiveDispatcher;

    protected void setup(SocketChannel channel) throws IOException {

        this.channel = channel;
        SocketChannelAdapter adapter = new SocketChannelAdapter(channel, ioContext.getIoSelector(), this);
        sender = adapter;
        receiver = adapter;

        sendDispatcher = new AsyncSendDispatcher(sender);
        receiveDispatcher = new AsyncReceiveDispatcher(receiver,receivePacketCallback);

        receiveDispatcher.start();

    }

    public void send(String msg){
        SendPacket packet = new StringSendPacket(msg);
        sendDispatcher.send(packet);
    }


    protected void onReceiveFromCore(String msg) {
        //System.out.println(msg);
    }



    //实现Closeable方法
    @Override
    public void close() throws IOException {
        sendDispatcher.close();
        receiveDispatcher.close();
        sender.close();
        receiver.close();
        channel.close();

    }

    //实现SocketChannelAdapter.OnChannelStatusChangedListener中的方法
    @Override
    public void onChannelClosed(SocketChannel channel) {

    }

    //接收到消息的回调
    //接收AsyncReceiveDispatcher中的回调
    private ReceiveDispatcher.ReceivePacketCallback receivePacketCallback = packet -> {
        if(packet instanceof StringReceivePacket){
            String msg = packet.toString();
            onReceiveFromCore(msg);
        }
    };
}
