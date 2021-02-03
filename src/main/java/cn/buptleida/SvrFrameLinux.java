package cn.buptleida;

import cn.buptleida.conf.CommandMapFactory;
import cn.buptleida.database.RedisServer;
import cn.buptleida.nio.IOService;
import cn.buptleida.persistence.AOF;
import redis.clients.jedis.Jedis;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class SvrFrameLinux {
    public static void main(String[] args) throws Exception {
        if(args.length!=0){
            inputStream(args[0]);
            return;
        }
        //读取配置文件
        Properties pps = getProperties();
        //初始化数据结构
        RedisServer.INSTANCE.init();
        RedisServer.INSTANCE.initDB(0);
        //AOF启动
        aofInit(pps);
        //初始化IO模块
        initNetwork(pps.getProperty("ip","127.0.0.1"),Integer.parseInt(pps.getProperty("port","8008")));
    }


    /**
     * 初始化IO模块
     * @param ConfigIp
     * @param ConfigPort
     */
    private static void initNetwork(String ConfigIp,int ConfigPort){
        System.out.println(ConfigIp+":"+ConfigPort);

        IOService ioService = new IOService(ConfigIp, ConfigPort);

        try {
            ioService.InitIOSelector();// 开启输入输出selector线程，开启读写单例线程池
            ioService.InitSocket();// 开启accept线程，监听客户端连接
        } catch (IOException e) {
            System.out.println("xxxxxxxxxxxxxxxxxxx Init SERVER FAILED xxxxxxxxxxxxxxxxxxxxx");
        }

        ioService.start();
    }

    /**
     * AOF持久化:数据恢复+启动
     */
    private static void aofInit(Properties pps){

        String aofPath = pps.getProperty("aofPath","/aof/");
        AOF.setAofFilePath(aofPath+"appendonly.aof");
        AOF.setCmdPath("/innerConf/modifyingCmd");
        try {
            AOF.recovery();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            System.out.println("AOF RECOVERY SUCCESS !");
        }
        AOF.startup();
    }

    /**
     * 读取配置文件
     * @return
     * @throws IOException
     */
    private static Properties getProperties() throws IOException {
        Properties pps = new Properties();
        pps.load(new FileInputStream("config.properties"));
        CommandMapFactory.setCmdTablePath("/innerConf/cmdTable.txt");
        return pps;
    }

    private static void inputStream(String param){
        if(param.equalsIgnoreCase("jedisTest")){
            long start,end;
            String ServerIp = "192.168.137.14";
            int ServerPort = 6379;
            Jedis jedis = new Jedis(ServerIp,ServerPort);
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

}
