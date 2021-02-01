package pressure;

// import dar.nio.core.ioContext;
// import dar.nio.impl.ioSelectorProvider;
// import dar.nio.IOClient;
import cn.buptleida.nio.IOClient;
import cn.buptleida.nio.core.ioContext;
import cn.buptleida.nio.impl.ioSelectorProvider;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.util.concurrent.CountDownLatch;

public class TimeConsumeTest {


    /**
     * Daredis测试
     * @throws IOException
     */
    @Test
    public void GetTest() throws IOException {
        System.out.println("--------------开始daredis的GET测试----------------");
        long start,end;
        ioContext.setIoSelector(new ioSelectorProvider());

        String ServerIp = "127.0.0.1";
        int ServerPort = 8008;
        // String ServerIp = "192.168.137.14";
        // int ServerPort = 8008;

        try {
            //CountDownLatch latch = new CountDownLatch(10000);
            IOClient connector = IOClient.startWith(ServerIp,ServerPort);
            // String name = "name"+System.currentTimeMillis();
            // System.out.println(name);
            // String str = readStrFromFile(name);
            // start = System.currentTimeMillis();
            // connector.sendMsg(str);
            // end = System.currentTimeMillis();
            // System.out.println("插入耗时："+(end-start));
            String name = "age";
            start = System.currentTimeMillis();
            for(int i=0;i<10001;++i){
                connector.sendMsg("GET "+name);
            }
            //latch.await();
            end = System.currentTimeMillis();
            System.out.println("查询耗时："+(end-start));
            System.out.println("QPS："+10000/((float)(end-start)/1000));
            connector.close();
        }catch (Exception e){
            System.out.println("连接失败，退出");
        }
        ioContext.close();
    }

    private String readStrFromFile(String name) throws IOException {
        File file = new File("D:\\Study\\project\\压测\\test.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder res = new StringBuilder("SET "+name+" ");
        String line;
        while((line=reader.readLine())!=null){
            res.append(line);
        }
        reader.close();
        return res.toString();

    }

    @Test
    public void generateFile() throws IOException {
        File file = new File("D:\\Study\\project\\压测\\test.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        for(int i=0;i<50000;++i){
            writer.write(Long.toString(System.currentTimeMillis()));
        }
        writer.close();
    }

    @Test
    public void jedisGetTest() throws IOException {
        long start,end;
        // String ServerIp = "127.0.0.1";
        String ServerIp = "192.168.137.14";
        int ServerPort = 6379;
        Jedis jedis = new Jedis(ServerIp,ServerPort);
        // String name = "name"+System.currentTimeMillis();
        // System.out.println(name);
        // String str = readStrFromFile(name);
        // start = System.currentTimeMillis();
        // jedis.set(name,str);
        // end = System.currentTimeMillis();
        // System.out.println("插入耗时："+(end-start));
        String name = "age";
        start = System.currentTimeMillis();
        for(int i=0;i<10000;++i){
            jedis.get(name);
        }
        end = System.currentTimeMillis();
        System.out.println("查询耗时："+(end-start));
        System.out.println("QPS："+10000/((float)(end-start)/1000));
    }

}
