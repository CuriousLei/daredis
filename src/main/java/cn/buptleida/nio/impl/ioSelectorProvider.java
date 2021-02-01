package cn.buptleida.nio.impl;

import cn.buptleida.nio.core.ioContext;
import cn.buptleida.nio.core.ioProvider;
import cn.buptleida.util.CloseUtil;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;
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
    // private final ExecutorService testHandlePool;
    // private Runnable writeRegisterWake = new Runnable() {
    //     @Override
    //     public void run() {
    //         writeSelector.wakeup();
    //     }
    // };
    // private Runnable readRegisterWake = new Runnable() {
    //     @Override
    //     public void run() {
    //         readSelector.wakeup();
    //     }
    // };

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
        // testHandlePool = Executors.newSingleThreadExecutor(new IoProviderThreadFactory("test-Thread-Pool"));

        //建立两个线程，执行输入和输出的select
        startRead();
        startWrite();

    }

    private void startRead() {
        Thread thread = new Thread("IoSelectorProvider ReadSelector Thread") {
            //private Boolean done = false;
            private void readExecute(){
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
                        readExecute();
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
            private void writeExecute(){
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
            }
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
                        writeExecute();
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

        return registerRead(channel, readSelector, inRegInput, inputHandler, handlerMap, SelectionKey.OP_READ) != null;
    }

    @Override
    public boolean registerOutput(SocketChannel channel, OutputHandler outputHandler) {

        return registerWrite(channel, writeSelector, inRegOutput, outputHandler, handlerMap, SelectionKey.OP_WRITE) != null;
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
        // System.out.println("进入wait");
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (locker) {
            if (locker.get()) {
                try {
                    // System.out.println("进入等待队列");
                    //暂停当前线程，直到被唤醒
                    locker.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static SelectionKey registerRead(SocketChannel channel, Selector selector, AtomicBoolean locker,
                                         Runnable ioCallback, HashMap<SelectionKey, Runnable> map,
                                         int ops) {
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (locker) {
            locker.set(true);
            try {
                //这时候select会立即返回，若是0，则进入waitSelection，然后进行locker.wait
                // selector.wakeup();
                test0Read(selector);

                SelectionKey key = null;
                if (channel.isRegistered()) {
                    // key = channel.keyFor(selector);
                    key = test1Read(channel,selector);
                    if (key != null) {
                        // key.interestOps(key.readyOps() | ops);
                        key=test2Read(key,ops);
                    }
                }
                //（注册write）如果已注册过read，还没注册write，此时key为null
                if (key == null) {
                    // key = channel.register(selector, ops);
                    key = test3Read(channel, selector, ops);
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
    private static SelectionKey registerWrite(SocketChannel channel, Selector selector, AtomicBoolean locker,
                                         Runnable ioCallback, HashMap<SelectionKey, Runnable> map,
                                         int ops) {
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (locker) {
            locker.set(true);
            try {
                //这时候select会立即返回，若是0，则进入waitSelection，然后进行locker.wait
                // selector.wakeup();
                test0Write(selector);

                SelectionKey key = null;
                if (channel.isRegistered()) {
                    // key = channel.keyFor(selector);
                    key = test1Write(channel,selector);
                    if (key != null) {
                        // key.interestOps(key.readyOps() | ops);
                        key=test2Write(key,ops);
                    }
                }
                //（注册write）如果已注册过read，还没注册write，此时key为null
                if (key == null) {
                    // key = channel.register(selector, ops);
                    key = test3Write(channel, selector, ops);
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
    private static void test0Read(Selector selector){
        // ioSelectorProvider provider = ioContext.getIoSelector();
        // provider.testHandlePool.execute(provider.readRegisterWake);
        selector.wakeup();
    }
    private static SelectionKey test1Read(SocketChannel channel,Selector selector){
        return channel.keyFor(selector);
    }
    private static SelectionKey test2Read(SelectionKey key,int ops){
        return key.interestOps(key.readyOps() | ops);
    }
    private static SelectionKey test3Read(SocketChannel channel,Selector selector,int ops) throws ClosedChannelException {
        return channel.register(selector, ops);
    }

    private static void test0Write(Selector selector){
        // ioSelectorProvider provider = ioContext.getIoSelector();
        // provider.testHandlePool.execute(provider.writeRegisterWake);
        selector.wakeup();
    }
    private static SelectionKey test1Write(SocketChannel channel,Selector selector){
        return channel.keyFor(selector);
    }
    private static SelectionKey test2Write(SelectionKey key,int ops){
        return key.interestOps(key.readyOps() | ops);
    }
    private static SelectionKey test3Write(SocketChannel channel,Selector selector,int ops) throws ClosedChannelException {
        return channel.register(selector, ops);
    }

    // private static SelectionKey registerTest(SocketChannel channel, Selector selector, AtomicBoolean locker,
    //                                  Runnable ioCallback, HashMap<SelectionKey, Runnable> map,
    //                                  int ops){
    //
    // }

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
