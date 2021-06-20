package cn.buptleida.nio;

import cn.buptleida.nio.clihdl.ClientHandler;
import cn.buptleida.nio.core.ioContext;
import cn.buptleida.nio.impl.SingleIOSelectorProvider;
import cn.buptleida.nio.impl.ioSelectorProvider;
import cn.buptleida.util.CloseUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class IOServiceSingle implements ClientHandler.ClientHandlerCallBack {
    private final String svrIpAddr;
    private final int svrPort;
    private Selector selector;
    private final ArrayList<ClientHandler> clientHandlerList;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    public IOServiceSingle(String svrIpAddr, int svrPort) {
        this.svrIpAddr = svrIpAddr;
        this.svrPort = svrPort;
        this.clientHandlerList = new ArrayList<>();
    }

    /**
     * 建立selector以及serverChannel，进行注册绑定，用于监听客户端连接请求
     *
     * @throws IOException
     */
    public void InitSocket() throws IOException {
        //开启一个selector
        selector = Selector.open();
        //开启一个channel用于监听客户端连接请求
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //设置为非阻塞
        serverSocketChannel.configureBlocking(false);
        //绑定本地ip端口
        serverSocketChannel.socket().bind(new InetSocketAddress(InetAddress.getByName(svrIpAddr), svrPort), 128);
        //将channel注册到selector上
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    /**
     * 关闭网络服务器
     *
     * @throws IOException
     */
    public void stop() throws IOException {
        for (ClientHandler client : clientHandlerList) {
            client.close();
        }
        clientHandlerList.clear();

        isClosed.set(true);
    }

    /**
     * daredis服务主循环
     */
    public void start() {
        try {
            SingleIOSelectorProvider provider = new SingleIOSelectorProvider();
            ioContext.setIoSelector(provider);
            while (!isClosed.get()) {
                processAcceptEvents();//处理监听连接的事件
                provider.processReadEvents();//处理读事件
                provider.processWriteEvents();//处理写事件
            }
            ioContext.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 监听客户端连接的方法
     * @throws IOException
     */
    private void processAcceptEvents() throws IOException {
        if (selector.selectNow() == 0) return;
        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            iterator.remove();
            if (key.isAcceptable()) {
                //获取用于监听连接的serverChannel
                ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                //通过accept获取socketChannel，对应于一个客户端
                SocketChannel socketChannel = serverChannel.accept();
                // socketChannel.socket().setReceiveBufferSize(32*1024);
                // socketChannel.socket().setSendBufferSize(32*1024);
                String uuid = UUID.randomUUID().toString();//为客户端生成唯一标识
                System.out.println("已接受连接client:" + uuid
                        + " /Addr:" + socketChannel.getRemoteAddress()
                        + " /Port:" + socketChannel.socket().getPort());
                //创建对象，用于处理客户端消息
                ClientHandler clientHandler = new ClientHandler(socketChannel, IOServiceSingle.this, uuid);
                clientHandlerList.add(clientHandler);
            }
        }
    }

    @Override
    public synchronized void ExitNotify(ClientHandler clientHandler) {
        for (ClientHandler client : clientHandlerList) {
            if (clientHandler == client) {
                clientHandlerList.remove(clientHandler);
                break;
            }
        }
    }

    @Override
    public void NewMsgCallBack(ClientHandler srcClient, String msg) {

    }
}
