package cn.buptleida;

import cn.buptleida.nio.IOClient;
import org.junit.Test;

import java.io.IOException;

public class ClientTest {

    @Test
    public void test() throws IOException {
        String ServerIp = "127.0.0.1";
        int ServerPort = 8008;
        String res = IOClient.sendMsgWithIpPort(ServerIp,ServerPort,"GET name");

        System.out.println(res);
    }
}
