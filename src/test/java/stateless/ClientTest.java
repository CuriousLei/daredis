package stateless;

import cn.buptleida.nio.IOClient;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ClientTest {

    @Test
    public void test() throws IOException {
        String ServerIp = "127.0.0.1";
        int ServerPort = 8008;
        String res = IOClient.sendMsgWithIpPort(ServerIp,ServerPort,"SET name leida");

        System.out.println(res);
    }
}
