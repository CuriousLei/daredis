package cn.buptleida;

import cn.buptleida.database.RedisServer;
// import cn.buptleida.netty.Server;
import cn.buptleida.nio.IOService;

import java.io.IOException;

public class SvrFrame {
    public static void main(String[] args) throws Exception {
        //初始化数据结构
        RedisServer.init();
        RedisServer.initDB(0);
        //初始化IO模块
        initNetwork(args[0],Integer.parseInt(args[1]));

    }

    private static void initNetwork(String ConfigIp,int ConfigPort){
        System.out.println(ConfigIp+":"+ConfigPort);

        IOService ioService = new IOService(ConfigIp, ConfigPort);

        try {
            ioService.InitIOSelector();
            ioService.InitSocket();
        } catch (IOException e) {
            System.out.println("xxxxxxxxxxxxxxxxxxx Init SERVER FAILED xxxxxxxxxxxxxxxxxxxxx");
        }

        ioService.start();
    }

}
