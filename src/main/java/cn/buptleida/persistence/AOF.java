package cn.buptleida.persistence;

import cn.buptleida.database.RedisClient;
import cn.buptleida.database.RedisServer;

import java.io.*;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class AOF {
    private static final String AOF_FILE_PATH = "C:\\_study\\repo\\daredis\\src\\main\\resources\\aof\\appendonly.aof";
    private static final String CMD_PATH = "C:\\_study\\repo\\daredis\\src\\main\\resources\\aof\\modifyingCmd";
    private static final Exception EXCEPTION = new Exception("AOF格式错误！");
    private static final AtomicBoolean isClosed = new AtomicBoolean(false);

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
            RedisServer.commandExecute(fakeClient, command);
        }
        reader.close();
    }

    public static void startup() {
        HashSet<String> commandsSet = new HashSet<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(CMD_PATH)));
            String cmd = null;
            while((cmd=reader.readLine())!=null){
                commandsSet.add(cmd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread aofWriteThread = new Thread("AOF Write Thread") {
            @Override
            public void run() {
                while (!isClosed.get()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(AOF_FILE_PATH), true));
                        String[] command = null;
                        while ((command = RedisServer.commandsQueue.poll()) != null) {
                            if(!commandsSet.contains(command[0])) continue;// 非修改性命令，不需要写入
                            writer.write(catToAOFCommand(command));
                        }
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        isClosed.set(true);
                    }
                }
            }
        };
        aofWriteThread.start();
    }

    /**
     * 将命令转化为aof文件格式的命令
     * @param command
     * @return
     */
    private static String catToAOFCommand(String[] command) {
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
}
