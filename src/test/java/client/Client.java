package client;

import cn.buptleida.nio.core.ioContext;
import cn.buptleida.nio.impl.ioSelectorProvider;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Client {
    private static final String ServerIp = "127.0.0.1";
    private static final int ServerPort = 8008;
    public static void main(String[] args)throws IOException {

        ioContext.setIoSelector(new ioSelectorProvider());

        try {
            NIOConnector connector = NIOConnector.startWith(ServerIp,ServerPort);
            //connector.send("hello");
            write(connector);
        }catch (Exception e){
            System.out.println("连接失败，退出");
        }


        ioContext.close();
    }

    /**
     * 输出流方法
     */
    public static void write(NIOConnector connector) throws IOException {
        //构建键盘输入流
        InputStream in = System.in;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));


        for(;;){
            String str = bufferedReader.readLine();//从键盘输入获取内容

            if(str.equalsIgnoreCase("exit")){
                break;
            }
            connector.send(str);
        }
        System.out.println("输出流关闭");
    }

}
