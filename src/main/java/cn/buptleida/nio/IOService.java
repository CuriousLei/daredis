package cn.buptleida.nio;

import cn.buptleida.nio.clihdl.ClientHandler;
import cn.buptleida.nio.core.ioContext;
import cn.buptleida.nio.impl.ioSelectorProvider;
import cn.buptleida.util.CloseUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.net.SocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

public class IOService implements ClientHandler.ClientHandlerCallBack {
    private final String svrIpAddr;
    private final int svrPort;
    //private ServerSocket serverSocket;
    private Selector selector;
    private ClientAcceptor clientAcceptor;
    private final ArrayList<ClientHandler> clientHandlerList;

    public IOService(String svrIpAddr, int svrPort) {
        this.svrIpAddr = svrIpAddr;
        this.svrPort = svrPort;
        this.clientHandlerList = new ArrayList<>();
    }
    /**
     * 建立selector以及serverChannel，进行注册绑定，用于监听客户端连接请求
     * @throws IOException
     */
    public void InitSocket() throws IOException {
        //开启一个selector
        selector = Selector.open();
        //开启一个channel用于监听客户端连接请求
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        // serverSocketChannel.socket().setReceiveBufferSize(64*1024*1024);
        // serverSocketChannel.socket().setPerformancePreferences(1,1,0);
        //设置为非阻塞
        serverSocketChannel.configureBlocking(false);
        //绑定本地ip端口
        serverSocketChannel.socket().bind(new InetSocketAddress(InetAddress.getByName(svrIpAddr), svrPort),128);
        //将channel注册到selector上
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }
    /**
     * 建立read和write的selector，用于监视后续的客户端channel;
     * 同时建立输入输出的线程池
     * @throws IOException
     */
    public void InitIOSelector() throws IOException {

        ioContext.setIoSelector(new ioSelectorProvider());
    }

    /**
     * 关闭网络服务器
     * @throws IOException
     */
    public void stop() throws IOException {
        for (ClientHandler client : clientHandlerList) {
            client.close();
        }
        clientHandlerList.clear();
        clientAcceptor.exit();
        //routeThreadExecutor.shutdown();

        //关闭核心
        ioContext.close();
    }

    /**
     * 开始监听客户端连接
     */
    public void start() {
        //创建线程监听客户端连接
        clientAcceptor = new ClientAcceptor(selector);
        clientAcceptor.start();
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
    /**
     * 监听客户端连接请求的线程
     */
    class ClientAcceptor extends Thread {
        private final Selector selector;
        private Boolean done = false;

        ClientAcceptor(Selector selector) {
            this.selector = selector;
        }

        @Override
        public void run() {
            super.run();
            try {
                do {
                    //selector监听通道是否就绪，未就绪时select()返回0
                    if(selector.select()==0){
                        if(done){
                            break;
                        }
                        continue;
                    }
                    //通过迭代器来遍历序列的对象
                    Iterator<SelectionKey> iterator =selector.selectedKeys().iterator();
                    while(iterator.hasNext()){
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if(key.isAcceptable()){
                            //获取用于监听连接的serverChannel
                            ServerSocketChannel serverChannel = (ServerSocketChannel)key.channel();
                            //通过accept获取socketChannel，对应于一个客户端
                            SocketChannel socketChannel = serverChannel.accept();

                            // socketChannel.socket().setReceiveBufferSize(32*1024);
                            // socketChannel.socket().setSendBufferSize(32*1024);

                            String uuid = UUID.randomUUID().toString();//为客户端生成唯一标识
                            System.out.println("已接受连接client:" + uuid
                                    + " /Addr:" + socketChannel.getRemoteAddress()
                                    + " /Port:" + socketChannel.socket().getPort());
                            //创建对象，用于处理客户端消息
                            ClientHandler clientHandler = new ClientHandler(socketChannel, IOService.this, uuid);

                            //这里只有一个线程，貌似加锁没必要
                            // synchronized (SvrFrame.this){
                            //     clientHandlerList.add(clientHandle);
                            // }
                        }
                    }



                } while (!done);
            } catch (Exception e) {
                if (!done) {
                    System.out.println("异常退出！");
                }
            }
        }

        void exit(){
            done = true;
            CloseUtil.close(selector);
        }
    }

    public static void main(String[] args) {
        System.out.println("测试以下");
    }
}
