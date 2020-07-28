package cn.buptleida;

import cn.buptleida.nio.IOService;

import java.io.IOException;

public class SvrFrame {
    public static void main(String[] args) throws Exception {
        String ConfigIp;
        int ConfigPort;

        ConfigIp = args[0];
        ConfigPort = Integer.parseInt(args[1]);
        System.out.println(ConfigIp+":"+ConfigPort);

        IOService ioService = new IOService(ConfigIp, ConfigPort);

        try {
            ioService.InitIOSelector();
            ioService.InitSocket();
        } catch (IOException e) {
            System.out.println("xxxxxxxxxxxxxxxxxxx Init SERVER FAILED xxxxxxxxxxxxxxxxxxxxx");
        }

        ioService.start();
        System.out.println("xxxxxxxxxxxxxxxxxxx CHAT SERVER is running xxxxxxxxxxxxxxxxxxxxx");
    }
}
