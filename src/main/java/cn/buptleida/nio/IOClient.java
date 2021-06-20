package cn.buptleida.nio;

import cn.buptleida.nio.core.Connector;
import cn.buptleida.nio.core.ioContext;
import cn.buptleida.nio.impl.SingleIOSelectorProvider;
import cn.buptleida.nio.impl.ioSelectorProvider;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

public class IOClient extends Connector {
    private ClientSendReceiver clientSendReceiver;
    private String res;
    private final Object lock = new Object();
    private final AtomicBoolean onRecieve = new AtomicBoolean(false);
    private CountDownLatch latch;
    IOClient(SocketChannel socketChannel, CountDownLatch latch) throws IOException{
        setup(socketChannel);
        this.latch=latch;
    }
    IOClient(SocketChannel socketChannel) throws IOException{
        setup(socketChannel);
    }
    IOClient(SocketChannel socketChannel,ClientSendReceiver clientSendReceiver) throws IOException{
        setup(socketChannel);
        this.clientSendReceiver = clientSendReceiver;
    }
    public String sendMsg(String msg) {
        this.send(msg);
        for(;;){
            if(onRecieve.get()) break;
        }
        onRecieve.set(false);
        // return res;
        return res;
    }

    @Override
    protected void onReceiveFromCore(String msg) {
        super.onReceiveFromCore(msg);
        //输出收到的消息
        //System.out.println("接收到："+msg);
        res = msg;
        onRecieve.set(true);
    }

    @Override
    public void onChannelClosed(SocketChannel channel) {
        super.onChannelClosed(channel);
        System.out.println("连接已关闭无法读取数据");
        ioContext.close();
    }

    public static IOClient startWith(String serverIp, int serverPort,CountDownLatch latch) throws IOException {
        ioContext.setIoSelector(new ioSelectorProvider());

        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress(InetAddress.getByName(serverIp), serverPort));

        System.out.println("客户端信息："+ socketChannel.getLocalAddress().toString()+":"+socketChannel.socket().getLocalPort());
        System.out.println("服务器信息："+socketChannel.getRemoteAddress().toString()+":"+socketChannel.socket().getPort());


        return new IOClient(socketChannel,latch);
    }
    public static IOClient startWith(String serverIp, int serverPort) throws IOException {
        ioContext.setIoSelector(new ioSelectorProvider());
        //ioContext.setIoSelector(new SingleIOSelectorProvider());

        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress(InetAddress.getByName(serverIp), serverPort));

        System.out.println("客户端信息："+ socketChannel.getLocalAddress().toString()+":"+socketChannel.socket().getLocalPort());
        System.out.println("服务器信息："+socketChannel.getRemoteAddress().toString()+":"+socketChannel.socket().getPort());


        return new IOClient(socketChannel);
    }

    public static String sendMsgWithIpPort(String serverIp, int serverPort, String msg) throws IOException{
        IOClient connector = IOClient.startWith(serverIp,serverPort);
        return connector.sendMsg(msg);
    }

    public static IOClient startWithSingle(String serverIp, int serverPort) throws IOException {
        SingleIOSelectorProvider provider = new SingleIOSelectorProvider();
        ioContext.setIoSelector(provider);

        ClientSendReceiver clientSendReceiver = new ClientSendReceiver(provider);
        clientSendReceiver.start();

        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress(InetAddress.getByName(serverIp), serverPort));

        System.out.println("客户端信息："+ socketChannel.getLocalAddress().toString()+":"+socketChannel.socket().getLocalPort());
        System.out.println("服务器信息："+socketChannel.getRemoteAddress().toString()+":"+socketChannel.socket().getPort());

        return new IOClient(socketChannel,clientSendReceiver);
    }

    /**
     * 客户端处理收发的线程
     */
    static class ClientSendReceiver extends Thread{
        private final SingleIOSelectorProvider provider;
        private boolean done = false;
        ClientSendReceiver(SingleIOSelectorProvider provider){
            this.provider = provider;
        }
        @Override
        public void run() {
            super.run();
            while(!done){
                try {
                    provider.processReadEvents();
                    provider.processWriteEvents();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        void exit(){
            done = true;
        }
    }

    @Override
    public void close() throws IOException {
        clientSendReceiver.exit();
        super.close();
    }
}
