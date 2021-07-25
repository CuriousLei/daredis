package cn.buptleida.nio.impl;


import cn.buptleida.nio.core.Receiver;
import cn.buptleida.nio.core.Sender;
import cn.buptleida.nio.core.ioArgs;
import cn.buptleida.nio.core.ioProvider;
import cn.buptleida.util.CloseUtil;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketChannelAdapter implements Sender, Receiver, Cloneable {
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final SocketChannel channel;
    private final ioProvider provider;
    private final OnChannelStatusChangedListener listener;//指向Connector对象,只有onChannelClosed方法

    // private ioArgs.IoArgsEventListener receiveIoEventListener;//2个都是指向Connector中的echoReceiveListener对象，可借此向Connector回调消息
    // private ioArgs.IoArgsEventListener sendIoEventListener;
    private ioArgs.IoArgsEventProcessor receiveIoEventProcessor;//可借此向Connector回调消息
    private ioArgs.IoArgsEventProcessor sendIoEventProcessor;

    // private ioArgs receiveArgsTemp;

    public SocketChannelAdapter(SocketChannel channel, ioProvider provider,
                                OnChannelStatusChangedListener listener) throws IOException {
        this.channel = channel;
        this.provider = provider;
        this.listener = listener;

        channel.configureBlocking(false);
    }

    @Override
    public void setReceiveListener(ioArgs.IoArgsEventProcessor processor) {
        receiveIoEventProcessor = processor;
    }

    @Override
    public boolean postReceiveAsync() throws IOException {
        if (isClosed.get()) {
            throw new IOException("Current channel is closed!");
        }
        return provider.registerInput(channel, inputHandler);
    }

    @Override
    public void setSendListener(ioArgs.IoArgsEventProcessor processor) {
        sendIoEventProcessor = processor;
    }

    @Override
    public boolean postSendAsync() throws IOException {
        if (isClosed.get()) {
            throw new IOException("Current channel is closed!");
        }
        return provider.registerOutput(channel, outputHandler);
    }


    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            // 解除注册回调
            provider.unRegisterInput(channel);
            provider.unRegisterOutput(channel);
            // 关闭
            CloseUtil.close(channel);
            // 回调当前Channel已关闭
            listener.onChannelClosed(channel);
        }
    }

    private final ioProvider.InputHandler inputHandler = new ioProvider.InputHandler() {
        @Override
        protected void handle() {
            if (isClosed.get()) {
                return;
            }
            ioArgs.IoArgsEventProcessor processor = receiveIoEventProcessor;
            long msgLen = 256;
            if (processor.isNewIoArgs()) {//如果是首包，先读取首部，获取数据长度
                ioArgs args = processor.providerIoArgs(4);
                try {
                    if (args.readFrom(channel) > 0) {
                        processor.onConsumeCompleted(args);
                    } else {
                        processor.onConsumeFailed(args, new IOException("Cannot read any data!"));
                    }
                    msgLen = processor.packetLength();
                } catch (IOException ignored) {
                    CloseUtil.close(SocketChannelAdapter.this);
                }
            }
            // 缓冲区大小根据消息大小确定
            ioArgs args = processor.providerIoArgs(msgLen);
            try {
                // 具体的读取操作
                if (args.readFrom(channel) > 0) {
                    // 读取完成回调
                    processor.onConsumeCompleted(args);
                    postReceiveAsync();
                } else {
                    processor.onConsumeFailed(args, new IOException("Cannot read any data!"));
                }
            } catch (IOException ignored) {
                CloseUtil.close(SocketChannelAdapter.this);
            }

        }
    };


    private final ioProvider.OutputHandler outputHandler = new ioProvider.OutputHandler() {
        @Override
        protected void handle() {
            if (isClosed.get()) {
                return;
            }
            ioArgs.IoArgsEventProcessor processor = sendIoEventProcessor;
            long msgLen = 256;
            if (processor.isNewIoArgs()) {//如果是首包，先写入数据长度
                ioArgs args = processor.providerIoArgs(4);
                try {
                    if (args.writeTo(channel) <= 0) {
                        processor.onConsumeFailed(args, new IOException("Cannot write any data!"));
                    }
                    msgLen = processor.packetLength();
                } catch (IOException ignored) {
                    CloseUtil.close(SocketChannelAdapter.this);
                }
            }
            ioArgs args = processor.providerIoArgs(msgLen);
            try {
                // 具体的写操作
                if (args.writeTo(channel) > 0) {
                    // 写操作完成回调
                    processor.onConsumeCompleted(args);
                } else {
                    processor.onConsumeFailed(args, new IOException("Cannot write any data!"));
                }
            } catch (IOException ignored) {
                CloseUtil.close(SocketChannelAdapter.this);
            }
        }

    };


    public interface OnChannelStatusChangedListener {
        void onChannelClosed(SocketChannel channel);
    }
}
