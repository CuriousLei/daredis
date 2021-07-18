package pressure;

import cn.buptleida.dataCoreObj.underObj.ZipList;
import cn.buptleida.dataCoreObj.underObj.zlentry;
import cn.buptleida.nio.IOClient;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class storagePerformanceTest {
    private static final String ServerIp = "127.0.0.1";
    private static final int ServerPort = 8008;
    private static final int testNum = 200;
    /**
     * daredis存储性能压测
     */
    @Test
    public void stringSaveTest() throws IOException {
        IOClient client = IOClient.startWith(ServerIp,ServerPort);
        String strVal = readValFromFile();
        //System.out.println(strVal.length());
        //strVal = strVal.substring(4, 8);
        //System.out.println(strVal);
        //System.out.println(Arrays.toString(strVal.toCharArray()));
        //List<String> list = getKeys(1);
        // for(int i=0;i<testNum;++i){
        //     String s = "SET "+list.get(i)+" "+strVal;
        //     System.out.println(s);
        //     client.sendMsg(s);
        // }
        client.sendMsg("SET","huamingTest",strVal);
    }
    @Test
    public void stringDeleteTest() throws IOException {
        IOClient client = IOClient.startWith(ServerIp,ServerPort);
        String strVal = readValFromFile();
        List<String> list = getKeys(testNum);
        for(int i=0;i<testNum;++i){
            client.sendMsg("DEL "+list.get(i)+" "+strVal);
        }
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
