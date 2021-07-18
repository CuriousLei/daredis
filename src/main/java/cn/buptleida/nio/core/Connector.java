package cn.buptleida.nio.core;

import cn.buptleida.nio.box.StringArrayReceivePacket;
import cn.buptleida.nio.box.StringArraySendPacket;
import cn.buptleida.nio.box.StringReceivePacket;
import cn.buptleida.nio.box.StringSendPacket;
import cn.buptleida.nio.impl.SocketChannelAdapter;
import cn.buptleida.nio.impl.async.AsyncReceiveDispatcher;
import cn.buptleida.nio.impl.async.AsyncSendDispatcher;
import com.sun.xml.internal.fastinfoset.util.StringArray;

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
        receiveDispatcher = new AsyncReceiveDispatcher(receiver, receivePacketCallback);

        receiveDispatcher.start();

    }

    /**
     * 发送消息
     * @param msg 消息内容
     */
    public void send(Object msg) {
        if (msg instanceof String) {
            sendDispatcher.send(new StringSendPacket((String) msg));
        } else if (msg instanceof String[]) {
            sendDispatcher.send(new StringArraySendPacket((String[]) msg));
        }
    }

    protected void onReceiveFromCore(String[] msg) {

    }

    protected void onReceiveFromCore(String msg) {

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
    private final ReceiveDispatcher.ReceivePacketCallback receivePacketCallback = packet -> {
        if (packet instanceof StringReceivePacket) {
            onReceiveFromCore(packet.toString());
        } else if (packet instanceof StringArrayReceivePacket) {
            onReceiveFromCore(((StringArrayReceivePacket) packet).getStrArr());
        }
    };
}
