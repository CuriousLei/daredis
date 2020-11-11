package cn.buptleida.nio;

import cn.buptleida.nio.core.Connector;
import cn.buptleida.nio.core.ioContext;
import cn.buptleida.nio.impl.ioSelectorProvider;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public class IOClient extends Connector {
    private String res;
    private final AtomicBoolean onRecieve = new AtomicBoolean(false);
    IOClient(SocketChannel socketChannel) throws IOException{
        setup(socketChannel);
    }

    @Override
    protected void onReceiveFromCore(String msg) {
        super.onReceiveFromCore(msg);
        //输出收到的消息
        System.out.println("接收到："+msg);
        res = msg;
        onRecieve.set(true);
        // onRecieve.notify();
        // onRecieve.set(false);
    }

    @Override
    public void onChannelClosed(SocketChannel channel) {
        super.onChannelClosed(channel);
        System.out.println("连接已关闭无法读取数据");
        ioContext.close();
    }

    public static IOClient startWith(String serverIp, int serverPort) throws IOException {
        ioContext.setIoSelector(new ioSelectorProvider());

        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress(InetAddress.getByName(serverIp), serverPort));

        System.out.println("客户端信息："+ socketChannel.getLocalAddress().toString()+":"+socketChannel.socket().getLocalPort());
        System.out.println("服务器信息："+socketChannel.getRemoteAddress().toString()+":"+socketChannel.socket().getPort());


        return new IOClient(socketChannel);
    }

    public String sendMsg(String msg){
        this.send(msg);
        for(;;){
            if(onRecieve.get()) break;
        }
        onRecieve.set(false);
        return res;
    }

    public static String sendMsgWithIpPort(String serverIp, int serverPort, String msg) throws IOException{
        IOClient connector = IOClient.startWith(serverIp,serverPort);
        return connector.sendMsg(msg);
    }

}
