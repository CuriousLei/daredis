package cn.buptleida.nio.clihdl;

import cn.buptleida.database.RedisClient;
import cn.buptleida.database.RedisServer;
import cn.buptleida.nio.core.Connector;

import java.io.IOException;
import java.nio.channels.SocketChannel;


public class ClientHandler extends Connector implements RedisClient.RedisClientCallBack {

    //private final Removable removable;
    private final String uid;
    private final ClientHandlerCallBack clientHandlerCallBack;

    private RedisClient client;

    public ClientHandler(SocketChannel socketChannel, ClientHandlerCallBack clientHandlerCallBack, String uid) throws IOException {
        this.uid = uid;
        this.clientHandlerCallBack = clientHandlerCallBack;

        this.client = new RedisClient();
        this.client.setClientHandlerCallBack(this);

        setup(socketChannel);
    }

    @Override
    public void RedisMsgCallBack(Object msg) {
        // redis执行完毕回调，执行输出注册
        write(msg);
    }

    public interface ClientHandlerCallBack {

        //用户退出的回调
        void ExitNotify(ClientHandler clientHandler);

        void NewMsgCallBack(ClientHandler clientHandler, String msg);

    }

    /**
     * 收到客户端消息，从nio里面回调，args里面包含buffer字节数组存储数据
     */
    @Override
    protected void onReceiveFromCore(String msg) {
        super.onReceiveFromCore(msg);
        try {
            if (msg.equalsIgnoreCase("exit")) {
                close();
            }
            RedisServer.INSTANCE.commandExecute(client, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * runnable处理里面异常退出的回调
     */
    @Override
    public void onChannelClosed(SocketChannel channel) {
        super.onChannelClosed(channel);
        exitSelf();
    }

    /**
     * 向客户端返回数据
     *
     * @param msg
     */
    private void write(Object msg) {
        //System.out.println("发送：" + msg);
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

        clientHandlerCallBack.ExitNotify(this);
        System.out.println("client:" + uid + " 已退出");
    }


    public String getUid() {
        return uid;
    }
}
