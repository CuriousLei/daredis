package cn.buptleida;

import cn.buptleida.nio.core.ioContext;
import cn.buptleida.nio.impl.ioSelectorProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Client {
    public static void main(String[] args)throws IOException {

        ioContext.setIoSelector(new ioSelectorProvider());

        String ServerIp = "127.0.0.1";
        int ServerPort = 8008;

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
    private static void write(NIOConnector connector) throws IOException {
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
