package cn.buptleida.persistence;

import cn.buptleida.database.RedisClient;
import cn.buptleida.database.RedisServer;
import cn.buptleida.util.IoThreadFactory;

import java.io.*;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

public class AOF {
    static String AOF_FILE_PATH;
    private static String CMD_PATH;
    private static final Exception EXCEPTION = new Exception("AOF格式错误！");
    private static final AtomicBoolean isClosed = new AtomicBoolean(false);
    public static ExecutorService AOFOutputPool;

    public static void recovery() throws Exception {
        File aofFile = new File(AOF_FILE_PATH);
        BufferedReader reader = new BufferedReader(new FileReader(aofFile));
        RedisClient fakeClient = new RedisClient();
        fakeClient.setFake(true);
        String cmdHead;
        while ((cmdHead = reader.readLine()) != null) {
            if (cmdHead.charAt(0) != '*') throw EXCEPTION;
            int cmdLen = Integer.parseInt(cmdHead.substring(1));
            String[] command = new String[cmdLen];
            for (int i = 0; i < cmdLen; ++i) {
                String strHead = reader.readLine();
                if (strHead.charAt(0) != '$') throw EXCEPTION;
                int strLen = Integer.parseInt(strHead.substring(1));
                String str = reader.readLine();
                if (strLen != str.length()) throw EXCEPTION;
                command[i] = str;
            }
            // RedisServer.INSTANCE.commandExecute(fakeClient, command);
            RedisServer.INSTANCE.commandExecuteProxy(fakeClient, command);
        }
        reader.close();
    }

    public static void startup() {
        AOFOutputPool = Executors.newSingleThreadExecutor(new IoThreadFactory("AOF-output-threadPool"));
    }

    /**
     * 将命令转化为aof文件格式的命令
     * @param command
     * @return
     */
    static String catToAOFCommand(String[] command) {
        StringBuilder stb = new StringBuilder("*");
        stb.append(command.length);
        stb.append("\r\n");
        for (String item : command) {
            stb.append('$');
            stb.append(item.length());
            stb.append("\r\n");
            stb.append(item);
            stb.append("\r\n");
        }
        return stb.toString();
    }


    public static void setAofFilePath(String aofFilePath) {
        AOF_FILE_PATH = aofFilePath;
    }

    public static void setCmdPath(String cmdPath) {
        CMD_PATH = cmdPath;
    }


}
