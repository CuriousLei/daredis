package cn.buptleida.nio.impl.async;

import cn.buptleida.nio.core.SendDispatcher;
import cn.buptleida.nio.core.SendPacket;
import cn.buptleida.nio.core.Sender;
import cn.buptleida.nio.core.ioArgs;
import cn.buptleida.util.CloseUtil;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncSendDispatcher implements SendDispatcher, cn.buptleida.nio.core.ioArgs.IoArgsEventProcessor {
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private Sender sender;
    private Queue<SendPacket> queue = new ConcurrentLinkedDeque<>();
    private AtomicBoolean isSending = new AtomicBoolean();
    private ioArgs ioArgs = new ioArgs();
    private SendPacket<?> packetTemp;
    //当前发送的packet大小以及进度
    private long total;
    private long position;

    private ReadableByteChannel channel;

    public AsyncSendDispatcher(Sender sender) {
        this.sender = sender;
        sender.setSendListener(this);
    }

    /**
     * connector将数据封装进packet后，调用这个方法
     *
     * @param packet
     */
    @Override
    public void send(SendPacket packet) {
        queue.offer(packet);//将数据放进队列中
        if (isSending.compareAndSet(false, true)) {
            sendNextPacket();
        }
    }

    @Override
    public void cancel(SendPacket packet) {

    }

    /**
     * 从队列中取数据
     *
     * @return
     */
    private SendPacket takePacket() {
        SendPacket packet = queue.poll();
        if (packet != null && packet.isCanceled()) {
            //已经取消不用发送
            return takePacket();
        }
        return packet;
    }

    private void sendNextPacket() {
        SendPacket temp = packetTemp;
        if (temp != null) {
            CloseUtil.close(temp);
        }
        SendPacket packet = packetTemp = takePacket();
        if (packet == null) {
            //队列为空，取消发送状态
            isSending.set(false);
            return;
        }

        total = packet.length();
        position = 0;

        sendCurrentPacket();
    }

    private void sendCurrentPacket() {

        if (position >= total) {
            completePacket(position == total);
            sendNextPacket();
            return;
        }

        try {
            //当前包没发送完，继续发送当前包，注册write
            sender.postSendAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * 一个packet发送完毕
     *
     * @param isSucceed
     */
    private void completePacket(boolean isSucceed) {
        SendPacket packet = this.packetTemp;
        if (packet == null) {
            return;
        }
        CloseUtil.close(packet, channel);
        packetTemp = null;
        channel = null;
        total = 0;
        position = 0;
    }

    private void closeAndNotify() {
        CloseUtil.close(this);
    }

    @Override
    public void close() {
        if (isClosed.compareAndSet(false, true)) {
            isSending.set(false);
            //异常导致的完成操作
            completePacket(false);
        }
    }

    /**
     * 将packet中的数据写入ioArgs
     *
     * @return
     */
    @Override
    public ioArgs providerIoArgs(long len) {
        ioArgs = new ioArgs(len);
        ioArgs args = ioArgs;
        if (channel == null) {
            //首包
            //将数据装入channel，open()返回inputStream类型
            channel = Channels.newChannel(packetTemp.open());
            args.setLimit(4);
            args.writeLength((int) packetTemp.length());//首包先写入数据长度
        } else {
            args.setLimit((int) Math.min(args.capacity(), total - position));
            try {
                int count = args.readFrom(channel);
                // System.out.println("args.readFrom: "+count);
                position += count;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        return args;
    }

    @Override
    public boolean isNewIoArgs() {
        return channel == null;
    }

    @Override
    public void onConsumeFailed(ioArgs args, Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onConsumeCompleted(ioArgs args) {
        sendCurrentPacket();
    }

    @Override
    public long packetLength() {
        return packetTemp == null ? 0 : packetTemp.length();
    }
}
