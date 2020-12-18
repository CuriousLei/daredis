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
     * 接收回调,来自writeHandler输出线程
     */
    // private ioArgs.IoArgsEventListener ioArgsEventListener = new ioArgs.IoArgsEventListener() {
    //     @Override
    //     public void onStarted(ioArgs args) {
    //
    //     }
    //
    //     @Override
    //     public void onCompleted(ioArgs args) {
    //         //继续发送当前包packetTemp，因为可能一个包没发完
    //         sendCurrentPacket();
    //     }
    // };


    /**
     * 将数据写入ioArgs
     *
     * @return
     */
    @Override
    public ioArgs providerIoArgs() {
        ioArgs args = ioArgs;
        if (channel == null) {
            //首包
            //将数据装入channel，open()返回inputStream类型
            channel = Channels.newChannel(packetTemp.open());
            args.setLimit(4);
            args.writeLength((int) packetTemp.length());
        } else {
            args.setLimit((int) Math.min(args.capacity(), total - position));

            try {
                int count = args.readFrom(channel);
                position += count;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return args;

        // args.startWriting();//将ioArgs缓冲区中的指针设置好
        //
        // if (position >= total) {
        //     sendNextPacket();
        //     return;
        // } else if (position == 0) {
        //     //首包，需要携带长度信息
        //     args.writeLength(total);
        // }
        //
        // byte[] bytes = packetTemp.bytes();
        // //把bytes的数据写入到IoArgs中
        // int count = args.readFrom(bytes, position);
        // position += count;
        //
        // //完成封装
        // args.finishWriting();//flip()操作
        // //向通道注册OP_write，将Args附加到runnable中；selector线程监听到就绪即可触发线程池进行消息发送
        //
        // sender.setSendListener(this);
        // return null;
    }

    @Override
    public void onConsumeFailed(ioArgs args, Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onConsumeCompleted(ioArgs args) {
        sendCurrentPacket();
    }
}
