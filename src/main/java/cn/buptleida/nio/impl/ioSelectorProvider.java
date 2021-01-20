package cn.buptleida.nio.impl;

import cn.buptleida.nio.core.ioProvider;
import cn.buptleida.util.CloseUtil;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ioSelectorProvider implements ioProvider {
    private final Selector readSelector;
    private final Selector writeSelector;

    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    // 是否处于输入通道的注册过程
    private final AtomicBoolean inRegInput = new AtomicBoolean(false);
    // 是否处于输出通道的注册过程
    private final AtomicBoolean inRegOutput = new AtomicBoolean(false);

    private final HashMap<SelectionKey, Runnable> handlerMap = new HashMap<>();

    private final ExecutorService inputHandlePool;
    private final ExecutorService outputHandlePool;

    public ioSelectorProvider() throws IOException {
        this.readSelector = Selector.open();
        this.writeSelector = Selector.open();

        //建立线程池
        // inputHandlePool = Executors.newFixedThreadPool(4,
        //         new IoProviderThreadFactory("IoProvider-Input-Thread-Pool"));
        // outputHandlePool = Executors.newFixedThreadPool(4,
        //         new IoProviderThreadFactory("IoProvider-Output-Thread-Pool"));
        inputHandlePool = Executors.newSingleThreadExecutor(new IoProviderThreadFactory("IoProvider-Input-Thread-Pool"));
        outputHandlePool = Executors.newSingleThreadExecutor(new IoProviderThreadFactory("IoProvider-Output-Thread-Pool"));
        //建立两个线程，执行输入和输出的select
        startRead();
        startWrite();

    }

    private void startRead() {
        Thread thread = new Thread("IoSelectorProvider ReadSelector Thread") {
            //private Boolean done = false;
            @Override
            public void run() {
                super.run();
                try {

                    while (!isClosed.get()) {
                        //int n = readSelector.select();
                        if (readSelector.select() == 0) {
                            //这里有一个等待操作，等待注册结束
                            waitSelection(inRegInput);
                            continue;
                        } else if(inRegInput.get()){
                             waitSelection(inRegInput);
                        }
                        Iterator<SelectionKey> iterator = readSelector.selectedKeys().iterator();
                        while (iterator.hasNext()) {
                            SelectionKey key = iterator.next();
                            iterator.remove();//此处格外重要
                            if (key.isValid()) {
                                // 取消继续对keyOps的监听
                                key.interestOps(key.readyOps() & ~SelectionKey.OP_READ);

                                //线程池执行read操作
                                inputHandlePool.execute(handlerMap.get(key));
                            }
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    private void startWrite() {
        Thread thread = new Thread("IoSelectorProvider WriteSelector Thread") {
            @Override
            public void run() {
                while (!isClosed.get()) {
                    try {
                        //int n = writeSelector.select();
                        if (writeSelector.select() == 0) {
                            waitSelection(inRegOutput);
                            continue;
                        } else if(inRegOutput.get()){
                             waitSelection(inRegOutput);
                         }

                        //System.out.println(n+"个write就绪");
                        Set<SelectionKey> selectionKeys = writeSelector.selectedKeys();
                        //System.out.println(selectionKeys.size()+"个通道就绪...");
                        for (SelectionKey selectionKey : selectionKeys) {
                            if (selectionKey.isValid()) {
                                selectionKey.interestOps(selectionKey.readyOps() & ~SelectionKey.OP_WRITE);

                                outputHandlePool.execute(handlerMap.get(selectionKey));
                            }
                        }
                        selectionKeys.clear();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    public void close() {
        //compareAndSet方法：当布尔值为expect，则将其换成update，成功返回true
        if (isClosed.compareAndSet(false, true)) {
            inputHandlePool.shutdown();
            outputHandlePool.shutdown();

            handlerMap.clear();

            readSelector.wakeup();
            writeSelector.wakeup();
            CloseUtil.close(readSelector, writeSelector);
        }
    }

    @Override
    public boolean registerInput(SocketChannel channel, InputHandler inputHandler) {

        return register(channel, readSelector, inRegInput, inputHandler, handlerMap, SelectionKey.OP_READ) != null;
    }

    @Override
    public boolean registerOutput(SocketChannel channel, OutputHandler outputHandler) {

        return register(channel, writeSelector, inRegOutput, outputHandler, handlerMap, SelectionKey.OP_WRITE) != null;
    }

    @Override
    public void unRegisterInput(SocketChannel channel) {
        unRegister(channel, readSelector, handlerMap);
    }

    @Override
    public void unRegisterOutput(SocketChannel channel) {
        unRegister(channel, writeSelector, handlerMap);
    }

    private static void waitSelection(final AtomicBoolean locker) {
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (locker) {
            if (locker.get()) {
                try {
                    //暂停当前线程，直到被唤醒
                    locker.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static SelectionKey register(SocketChannel channel, Selector selector, AtomicBoolean locker,
                                         Runnable ioCallback, HashMap<SelectionKey, Runnable> map,
                                         int ops) {
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (locker) {
            locker.set(true);
            try {
                //这时候select会立即返回，若是0，则进入waitSelection，然后进行locker.wait
                selector.wakeup();

                SelectionKey key = null;
                if (channel.isRegistered()) {
                    key = channel.keyFor(selector);
                    if (key != null) {
                        key.interestOps(key.readyOps() | ops);
                    }
                }
                //（注册write）如果已注册过read，还没注册write，此时key为null
                if (key == null) {
                    key = channel.register(selector, ops);
                    map.put(key, ioCallback);
                }
                return key;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                //System.out.println("注册成功！");
                locker.set(false);
                //唤醒locker.wait()
                locker.notify();
            }
        }
    }

    private static void unRegister(SocketChannel channel, Selector selector, HashMap<SelectionKey, Runnable> map) {
        if (channel.isRegistered()) {
            SelectionKey key = channel.keyFor(selector);
            if(key!=null){
                key.cancel();
                map.remove(key);
            }
            selector.wakeup();
        }
    }


    static class IoProviderThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        IoProviderThreadFactory(String namePrefix) {
            SecurityManager s = System.getSecurityManager();
            this.group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            this.namePrefix = namePrefix;
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
