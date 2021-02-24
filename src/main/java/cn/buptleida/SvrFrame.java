package cn.buptleida;

import cn.buptleida.conf.CommandMapFactory;
import cn.buptleida.database.RedisServer;
import cn.buptleida.netty.Server;
import cn.buptleida.nio.IOService;
import cn.buptleida.persistence.AOF;

import java.io.IOException;

public class SvrFrame {
    public static void main(String[] args) throws Exception {
        CommandMapFactory.setCmdTablePath("C:\\_study\\repo\\daredis\\src\\main\\resources\\innerConf\\cmdTable.txt");
        //初始化数据结构
        RedisServer.INSTANCE.init();
        RedisServer.INSTANCE.initDB(0);
        //AOF启动
        aofInit();
        //初始化IO模块
        initNetwork(args[0],Integer.parseInt(args[1]));
        //初始化netty模块
        // initNetty(Integer.parseInt(args[1]));
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
     * 初始化netty模块
     * @param ConfigPort
     */
    private static void initNetty(int ConfigPort){
        new Server(ConfigPort).run();
    }

    /**
     * AOF持久化:数据恢复+启动
     */
    private static void aofInit(){
        AOF.setAofFilePath("C:\\_study\\repo\\daredis\\src\\main\\resources\\aof\\appendonly.aof");
        AOF.setCmdPath("C:\\_study\\repo\\daredis\\src\\main\\resources\\innerConf\\modifyingCmd");
        try {
            AOF.recovery();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            System.out.println("AOF RECOVERY SUCCESS !");
        }
        AOF.startup();
        RedisServer.INSTANCE.AofOpen = true;
    }
}
