package cn.buptleida.nio.clihdl;

import cn.buptleida.nio.core.Connector;
import cn.buptleida.nio.core.ioArgs;
import cn.buptleida.util.CloseUtil;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ClientHandler extends Connector {

    //private final Removable removable;
    private final String uid;
    private final ClientHandlerCallBack clientHandlerCallBack;

    public ClientHandler(SocketChannel socketChannel, ClientHandlerCallBack clientHandlerCallBack, String uid) throws IOException {
        this.uid = uid;
        this.clientHandlerCallBack = clientHandlerCallBack;
        setup(socketChannel);

    }

    public interface ClientHandlerCallBack {

        //用户退出的回调
        void ExitNotify(ClientHandler clientHandler);

        void NewMsgCallBack(ClientHandler clientHandler, String msg);

        //用户传来新消息的回调,NIO模式
        //void NewMsgCallBack(ClientHandler clientHandler, ioArgs args);
    }
    /**
     * 收到客户端消息，从nio里面回调，args里面包含buffer字节数组存储数据
     */
    @Override
    protected void onReceiveFromCore(String msg) {
        super.onReceiveFromCore(msg);

        System.out.println("收到："+msg);

        clientHandlerCallBack.NewMsgCallBack(ClientHandler.this, msg);

        //回调完再次注册，读取下一条数据
        //必须要这样
        //ioContext.getIoSelector().registerInput(socketChannel, ClientHandler.this);
    }
    /**
     * runnable处理里面异常退出的回调
     */
    @Override
    public void onChannelClosed(SocketChannel channel) {
        super.onChannelClosed(channel);
        exitSelf();
    }


    public void write(String msg) {
        System.out.println("发送："+msg);
        send(msg);
    }


    @Override
    public void close() throws IOException {
        super.close();
        exitSelf();
    }

    /**
     * clientHandler退出
     * 关闭套接字通道，把自身从对象列表中清除掉
     */
    private void exitSelf() {
        exit();
        clientHandlerCallBack.ExitNotify(this);
    }

    public void exit() {
        CloseUtil.close(this);
        System.out.println("客户端已退出");
    }


    public String getUid() {
        return uid;
    }
}
