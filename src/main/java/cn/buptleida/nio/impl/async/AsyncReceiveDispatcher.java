package cn.buptleida.nio.impl.async;

import cn.buptleida.nio.box.StringArrayReceivePacket;
import cn.buptleida.nio.core.ReceiveDispatcher;
import cn.buptleida.nio.core.ReceivePacket;
import cn.buptleida.nio.core.Receiver;
import cn.buptleida.nio.core.ioArgs;
import cn.buptleida.util.CloseUtil;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncReceiveDispatcher implements ReceiveDispatcher, ioArgs.IoArgsEventProcessor {
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final Receiver receiver;
    private final ReceivePacketCallback callback;
    private ioArgs args = new ioArgs();
    private ReceivePacket<?> packetTemp;
    private byte[] buffer;
    private long total;
    private long position;

    private WritableByteChannel channel;

    public AsyncReceiveDispatcher(Receiver receiver, ReceivePacketCallback callback) {
        this.receiver = receiver;
        this.receiver.setReceiveListener(this);
        this.callback = callback;
    }

    /**
     * connector中调用该方法进行
     */
    @Override
    public void start() {
        registerReceive();
    }

    private void registerReceive() {
        try {
            receiver.postReceiveAsync();
        } catch (IOException e) {
            closeAndNotify();
        }
    }

    private void closeAndNotify() {
        CloseUtil.close(this);
    }

    @Override
    public void stop() {

    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            completePacket(false);
        }
    }


    /**
     * 解析数据到packet
     *
     * @param args
     */
    private void assemblePacket(ioArgs args) {
        if (packetTemp == null) {//首包
            int length = args.readLength();//消息体长度
            //packetTemp = new StringReceivePacket(length);
            packetTemp = new StringArrayReceivePacket(length);
            //packetTemp.open()是获取一个输出流ByteArrayOutputStream
            channel = Channels.newChannel(packetTemp.open());

            buffer = new byte[length];
            total = length;
            position = 0;
        }


        try {
            int count = args.writeTo(channel);

            position += count;

            if (position == total) {
                completePacket(true);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void completePacket(boolean isSucceed) {
        ReceivePacket packet = this.packetTemp;
        CloseUtil.close(packet);
        packetTemp = null;

        WritableByteChannel channel = this.channel;
        CloseUtil.close(channel);
        this.channel = null;
        if (packet != null)
            callback.onReceivePacketCompleted(packet);
    }

    //下面三个方法实现ioArgs中的接口IoArgsEventProcessor
    @Override
    public ioArgs providerIoArgs() {
        ioArgs args = this.args;
        int receiveSize;
        if (packetTemp == null) {
            receiveSize = 4;
        } else {
            receiveSize = (int) Math.min(total - position, args.capacity());
        }
        //设置接受数据大小，这一步很关键，通过设置limit将缓冲区分割，从而解决粘包问题
        // System.out.println("接收数据大小："+receiveSize);
        args.setLimit(receiveSize);
        return args;
    }

    @Override
    public boolean isNewIoArgs() {
        return packetTemp == null;
    }

    @Override
    public void onConsumeFailed(ioArgs args, Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onConsumeCompleted(ioArgs args) {
        assemblePacket(args);// 从buffer中读取数据
        // registerReceive();// 如果不是首包，且数据未读取完，继续注册receive
    }

}
