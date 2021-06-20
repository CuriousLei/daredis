package cn.buptleida.nio.impl;

import cn.buptleida.nio.core.ioProvider;
import cn.buptleida.util.CloseUtil;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class SingleIOSelectorProvider implements ioProvider {
    private final Selector readSelector;
    private final Selector writeSelector;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final HashMap<SelectionKey, Runnable> handlerMap = new HashMap<>();

    public SingleIOSelectorProvider() throws IOException {
        this.readSelector = Selector.open();
        this.writeSelector = Selector.open();
    }
    public void processReadEvents() throws IOException{
        // 从readSelector中获取读事件
        if (readSelector.selectNow() == 0) return;
        Iterator<SelectionKey> readIterator = readSelector.selectedKeys().iterator();
        while (readIterator.hasNext()) {
            SelectionKey key = readIterator.next();
            readIterator.remove();
            if (key.isValid()) {
                key.interestOps(key.readyOps() & ~SelectionKey.OP_READ);
                Runnable rb = handlerMap.get(key);
                rb.run();
            }
        }
    }
    public void processWriteEvents() throws IOException{
        // 从writeSelector中获取写事件
        if (writeSelector.selectNow() == 0) return;
        Iterator<SelectionKey> writeIterator = writeSelector.selectedKeys().iterator();
        while (writeIterator.hasNext()) {
            SelectionKey key = writeIterator.next();
            writeIterator.remove();
            if (key.isValid()) {
                key.interestOps(key.readyOps() & ~SelectionKey.OP_WRITE);
                Runnable rb = handlerMap.get(key);
                rb.run();
            }
        }
    }

    @Override
    public boolean registerInput(SocketChannel channel, InputHandler inputHandler) {
        return register(channel, readSelector, inputHandler, handlerMap, SelectionKey.OP_READ) != null;
    }

    @Override
    public boolean registerOutput(SocketChannel channel, OutputHandler outputHandler) {
        return register(channel, writeSelector, outputHandler, handlerMap, SelectionKey.OP_WRITE) != null;
    }

    @Override
    public void unRegisterInput(SocketChannel channel) {
        unRegister(channel, readSelector, handlerMap);
    }

    @Override
    public void unRegisterOutput(SocketChannel channel) {
        unRegister(channel, writeSelector, handlerMap);
    }

    @Override
    public void close() {
        //compareAndSet方法：当布尔值为expect，则将其换成update，成功返回true
        if (isClosed.compareAndSet(false, true)) {
            handlerMap.clear();
            readSelector.wakeup();
            writeSelector.wakeup();
            CloseUtil.close(readSelector, writeSelector);
        }
    }

    private static SelectionKey register(SocketChannel channel, Selector selector,
                                             Runnable ioCallback, HashMap<SelectionKey, Runnable> map,
                                             int ops) {
        try {
            SelectionKey key = null;
            if (channel.isRegistered()) {
                key = channel.keyFor(selector);
                if (key != null) {
                    key.interestOps(key.readyOps() | ops);
                }
            }
            if (key == null) {
                key = channel.register(selector, ops);
                map.put(key, ioCallback);
            }
            return key;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void unRegister(SocketChannel channel, Selector selector, HashMap<SelectionKey, Runnable> map) {
        if (channel.isRegistered()) {
            SelectionKey key = channel.keyFor(selector);
            if (key != null) {
                key.cancel();
                map.remove(key);
            }
            selector.wakeup();
        }
    }
}
