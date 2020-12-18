package stateless;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class DarConnector {
    private static DarConnector instance = new DarConnector();
    private OutputStream outputStream;
    private InputStream inputStream;

    private DarConnector(){}

    public static DarConnector startWith(String serverIp, int serverPort) throws IOException {
        Socket socket = new Socket();
        //socket.setSoTimeout(3000);
        socket.connect(new InetSocketAddress(serverIp,serverPort),3000);

        instance.outputStream = socket.getOutputStream();
        instance.inputStream = socket.getInputStream();
        return instance;
    }

    public String sendMsg(String msg) throws IOException {
        PrintStream printStream = new PrintStream(outputStream);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        printStream.println(msg);
        return bufferedReader.readLine();
    }
}
