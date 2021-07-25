package pressure;

import cn.buptleida.nio.IOClient;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class storagePerformanceTest {
    private static final String ServerIp = "127.0.0.1";
    private static final int ServerPort = 8008;
    private static final int testNum = 80000;
    /**
     * daredis存储性能压测
     */
    @Test
    public void stringSaveTest() throws IOException {
        //IOClient client = IOClient.startWith(ServerIp,ServerPort);
        IOClient client = IOClient.startWithSingle(ServerIp,ServerPort);
        String strVal = readValFromFile();

        List<String> list = getKeys(testNum);
        long start = System.currentTimeMillis();
        for(int i=0;i<testNum;++i){
            String[] s = new String[]{"SET",list.get(i),strVal};
            //System.out.println(Arrays.toString(s));
            client.sendMsg(s);
        }
        long end = System.currentTimeMillis();
        System.out.println((float)testNum/((float)(end-start)/1000));
        //client.sendMsg("SET","huamingTest",strVal);
    }
    @Test
    public void stringDeleteTest() throws IOException {
        //IOClient client = IOClient.startWith(ServerIp,ServerPort);
        IOClient client = IOClient.startWithSingle(ServerIp,ServerPort);
        List<String> list = getKeys(testNum);
        long start = System.currentTimeMillis();
        for(int i=0;i<testNum;++i){
            client.sendMsg("DEL",list.get(i));
        }
        long end = System.currentTimeMillis();
        System.out.println((float)testNum/((float)(end-start)/1000));
    }

    @Test
    public void jedisSaveTest() throws IOException {
        long start,end;
        String ServerIp = "127.0.0.1";
        int ServerPort = 6379;
        Jedis jedis = new Jedis(ServerIp,ServerPort);
        String strVal = readValFromFile();
        List<String> list = getKeys(testNum);
        start = System.currentTimeMillis();
        for(int i=0;i<testNum;++i){
            jedis.set(list.get(i),strVal);
        }
        end = System.currentTimeMillis();
        System.out.println("插入耗时："+(end-start));
        System.out.println("QPS："+testNum/((float)(end-start)/1000));
    }

    @Test
    public void jedisDeleteTest() throws IOException {
        long start,end;
        String ServerIp = "127.0.0.1";
        int ServerPort = 6379;
        Jedis jedis = new Jedis(ServerIp,ServerPort);
        List<String> list = getKeys(testNum);
        start = System.currentTimeMillis();
        for(int i=0;i<testNum;++i){
            jedis.del(list.get(i));
        }
        end = System.currentTimeMillis();
        System.out.println("插入耗时："+(end-start));
        System.out.println("QPS："+testNum/((float)(end-start)/1000));
    }



    private String readValFromFile() throws IOException {
        File file = new File("D:\\Study\\project\\压测\\item.json");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder res = new StringBuilder();
        String line;
        while((line=reader.readLine())!=null){
            res.append(line);
        }
        reader.close();
        return res.toString();

    }

    /**
     * 程序生成一堆keys存入文件
     * @throws IOException
     */
    @Test
    public void generateKeys() throws IOException {
        File file = new File("D:\\Study\\project\\压测\\keys.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        long time = System.currentTimeMillis();
        for(int i=0;i<100000;++i){
            writer.write(Long.toString(time++));
            writer.newLine();
        }
        writer.close();
    }

    private List<String> getKeys(int num) throws IOException{
        File file = new File("D:\\Study\\project\\压测\\keys.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        List<String> res = new ArrayList<>(num);
        for(int i = 0;i<num;++i){
            String line = reader.readLine();
            if(line==null) break;
            res.add(line);
        }
        reader.close();
        return res;
    }


}
