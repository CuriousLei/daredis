package cn.buptleida.database;

import cn.buptleida.conf.Command;
import cn.buptleida.conf.CommandMapFactory;
import cn.buptleida.conf.Toast;
import cn.buptleida.structure.base.CmdExecutor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public enum RedisServer {
    INSTANCE;
    private int dbNum = 16;
    public RedisDB[] db;
    public ConcurrentLinkedDeque<String[]> commandsQueue;
    public HashMap<String, Command> commandsMap;
    public boolean AofOpen = false;

    public void init() throws IOException {
        db = new RedisDB[dbNum];
        commandsQueue = new ConcurrentLinkedDeque<>();
        commandsMap = CommandMapFactory.getCmdMap();
    }

    public void initDB(int index) {
        if (db[index] == null) db[index] = new RedisDB();
    }

    /**
     * 执行命令
     *
     * @param client
     * @param commandStr
     * @return
     * @throws Exception
     */
    public String commandExecute(RedisClient client, String commandStr) throws Exception {
        String[] command = commandStr.split(" ");
        commandExecuteProxy(client, command);
        return null;
    }

    public String commandExecute(RedisClient client, String[] command) throws Exception {
        System.out.println(Arrays.toString(command));
        String commandName = command[0];
        String[] params = Arrays.copyOfRange(command, 1, command.length);
        if (commandName.equalsIgnoreCase("select")) {
            int index = Integer.parseInt(params[0]);
            initDB(index);
            client.setDb(db[index]);
            return "SWITCHED TO DB" + index;
        }
        Object database = client.getDb();
        Class<RedisDB> redisDBClass = RedisDB.class;
        Method method = redisDBClass.getMethod(commandName, String[].class);
        Object returnValue = method.invoke(database, (Object) params);

        if (!client.isFake()) commandsQueue.offer(command);
        return toStr(returnValue);
    }

    public void commandExecuteProxy(RedisClient client, String[] params) throws Exception {

        String commandName = params[0];
        Command cmd = commandsMap.get(commandName);
        RedisDB redisDB = client.getDb();
        Object executor;
        if ((cmd.getLevel() & 1) == 1) {//对象级命令
            executor = redisDB.getExecutorByKey(params[1]);
            if (executor == null) {
                client.msgReturn(Toast.NOT_EXIST);
                return;
            }
        } else if ((cmd.getLevel() & 2) == 2) {//键空间级命令
            executor = redisDB;
        } else {//数据库级命令
            executor = this;
        }
        Method method = executor.getClass().getMethod(cmd.getMethod(), cmd.getParamClasses());
        ProxyInvocationHandler pih = new ProxyInvocationHandler();//创建代理器
        pih.setTarget(executor);//注入目标对象
        CmdExecutor proxy = (CmdExecutor) pih.getProxy();//得到代理对象
        proxy.proc(params, cmd, method, client);//执行命令
    }

    private String toStr(Object obj) {
        if (obj instanceof Integer) {
            return Integer.toString((Integer) obj);
        } else if (obj instanceof Long) {
            return Long.toString((Long) obj);
        } else if (obj instanceof String) {
            return (String) obj;
        }
        return null;
    }

    public static void main(String[] args) throws Exception {

        RedisServer.INSTANCE.init();
        RedisServer.INSTANCE.initDB(0);

        RedisClient client = new RedisClient();
        double start = System.currentTimeMillis();
        for (int i = 0; i < 1000; ++i) {
            RedisServer.INSTANCE.commandExecute(client, "HSET myHt key" + i + " " + i);
        }
        double end = System.currentTimeMillis();
        System.out.println(end - start);

        start = System.currentTimeMillis();
        for (int i = 0; i < 1000; ++i) {
            RedisServer.INSTANCE.commandExecute(client, "HGET myHt key" + i);
        }
        end = System.currentTimeMillis();
        System.out.println(end - start);

    }
}
