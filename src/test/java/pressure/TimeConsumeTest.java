package pressure;

import client.Client;
import client.NIOConnector;
import cn.buptleida.nio.IOClient;
import cn.buptleida.nio.core.ioContext;
import cn.buptleida.nio.impl.ioSelectorProvider;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.io.*;

public class TimeConsumeTest {


    /**
     * Daredis测试
     * @throws IOException
     */
    @Test
    public void GetTest() throws IOException {
        long start,end;
        ioContext.setIoSelector(new ioSelectorProvider());

        String ServerIp = "127.0.0.1";
        int ServerPort = 8008;

        try {
            IOClient connector = IOClient.startWith(ServerIp,ServerPort);
            String name = "name"+System.currentTimeMillis();
            System.out.println(name);
            String str = readStrFromFile(name);
            start = System.currentTimeMillis();
            connector.sendMsg(str);
            end = System.currentTimeMillis();
            System.out.println("插入耗时："+(end-start));
            start = System.currentTimeMillis();
            for(int i=0;i<1;++i){
                connector.sendMsg("GET "+name);
            }
            end = System.currentTimeMillis();
            System.out.println("查询耗时："+(end-start));
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
        String ServerIp = "127.0.0.1";
        int ServerPort = 6379;
        Jedis jedis = new Jedis(ServerIp,ServerPort);
        String name = "name"+System.currentTimeMillis();
        System.out.println(name);
        String str = readStrFromFile(name);
        start = System.currentTimeMillis();
        jedis.set(name,str);
        end = System.currentTimeMillis();
        System.out.println("插入耗时："+(end-start));
        start = System.currentTimeMillis();
        System.out.println(jedis.get(name));
        end = System.currentTimeMillis();
        System.out.println("查询耗时："+(end-start));
    }

}
