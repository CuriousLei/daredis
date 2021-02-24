package pressure;

import cn.buptleida.nio.IOClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class multiThreadPressureTest {
    private static String ServerIp = "127.0.0.1";
    private static int ServerPort = 8008;
    private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws InterruptedException {
        List<IOClient> ClientList = new ArrayList<>();
        for (int i = 0; i < 2; ++i) {
            System.out.println(i);
            try {
                IOClient client = IOClient.startWith(ServerIp,ServerPort);
                if (client == null) {
                    throw new NullPointerException();
                }
                ClientList.add(client);
            } catch (IOException |NullPointerException e) {
                System.out.println("创建客户端失败");
            }

            //定个延时
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        CountDownLatch latch = new CountDownLatch(10000);
        long start = System.currentTimeMillis();
        for(IOClient client : ClientList){
            Runnable runnable = () -> {
                for(int i=0;i<5000;++i){
                    client.sendMsg("GET name");
                    latch.countDown();
                }
            };
            executorService.execute(runnable);
        }
        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("查询耗时："+(end-start));
        System.out.println("QPS："+10000/((float)(end-start)/1000));
    }
}
