package cn.buptleida;

import cn.buptleida.database.RedisServer;
import cn.buptleida.nio.IOService;
import cn.buptleida.persistence.AOF;

import java.io.IOException;

public class SvrFrame {
    public static void main(String[] args) throws Exception {
        //初始化数据结构
        RedisServer.init();
        RedisServer.initDB(0);
        //AOF启动
        aofInit();
        //初始化IO模块
        initNetwork(args[0],Integer.parseInt(args[1]));

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
    private static void aofInit(){
        try {
            AOF.recovery();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            System.out.println("AOF RECOVERY SUCCESS !");
        }
        AOF.startup();
    }
}
