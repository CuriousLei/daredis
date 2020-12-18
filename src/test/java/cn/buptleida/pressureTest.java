package cn.buptleida;

import cn.buptleida.nio.core.ioContext;
import cn.buptleida.nio.impl.ioSelectorProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class pressureTest {
    private static boolean done;
    public static void main(String[] args) throws IOException {
        ioContext.setIoSelector(new ioSelectorProvider());

        String ServerIp = "127.0.0.1";
        int ServerPort = 8008;

        List<NIOConnector> ClientList = new ArrayList<>();
        for (int i = 0; i < 100; ++i) {
            System.out.println(i);
            try {
                NIOConnector client = NIOConnector.startWith(ServerIp,ServerPort);
                if (client == null) {
                    throw new NullPointerException();
                }
                ClientList.add(client);
            } catch (IOException|NullPointerException e) {
                System.out.println("创建客户端失败");
            }

            //定个延时
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        systemRead();

        Runnable runnable = () -> {
            while (!done) {
                int i=0;
                long now = System.currentTimeMillis();
                for (NIOConnector client : ClientList) {
                    //client.send("SET "+"name"+i+" "+i*i);
                    client.send("GET name");
                    i++;
                }
                // try {
                //     Thread.sleep(1000);
                // } catch (InterruptedException e) {
                //     e.printStackTrace();
                // }
                long end = System.currentTimeMillis();
                System.out.println(end-now);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();

        systemRead();
        //结束
        done=true;

        //主线程等待runnable线程完成
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (NIOConnector client : ClientList) {
            try {
                client.close();
            } catch (IOException e) {
                System.out.println("关闭失败");
            }
        }
        ClientList.clear();

        ioContext.close();
    }
    static void systemRead() {
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
