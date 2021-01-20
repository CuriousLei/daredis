package cn.buptleida.database;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedDeque;

public class RedisServer {

    private static int dbNum = 16;
    public static RedisDB[] db;
    public static ConcurrentLinkedDeque<String[]> commandsQueue;
    public static void init(){
        db = new RedisDB[dbNum];
        commandsQueue = new ConcurrentLinkedDeque<>();
    }

    public static void initDB(int index){
        if(db[index]==null) db[index] = new RedisDB();
    }

    /**
     * 执行命令的方法
     * @param client
     * @param commandStr
     * @return
     * @throws Exception
     */
    public static String commandExecute(RedisClient client, String commandStr) throws Exception {
        String[] command = commandStr.split(" ");
        return commandExecute(client,command);
    }
    public static String commandExecute(RedisClient client, String[] command) throws Exception {

        String commandName = command[0];
        String[] params = Arrays.copyOfRange(command,1,command.length);
        if(commandName.equalsIgnoreCase("select")){
            int index = Integer.parseInt(params[0]);
            initDB(index);
            client.setDb(db[index]);
            return "SWITCHED TO DB"+index;
        }
        Object database = client.getDb();
        Class<RedisDB> redisDBClass = RedisDB.class;
        Method method = redisDBClass.getMethod(commandName, String[].class);
        Object returnValue =  method.invoke(database,(Object) params);

        if(!client.isFake()) commandsQueue.offer(command);
        return toStr(returnValue);
    }

    private static String toStr(Object obj){
        if(obj instanceof Integer){
            return Integer.toString((Integer) obj);
        }else if(obj instanceof Long){
            return Long.toString((Long) obj);
        }else if(obj instanceof String){
            return (String) obj;
        }
        return null;
    }
    public void test(String ... strs){
        String key = strs[0];
        String val = strs[1];
        System.out.println(key+" "+val+" "+strs[2]);
    }
    public static void main(String[] args) throws Exception{
        RedisServer.init();
        RedisServer.initDB(0);

        RedisClient client = new RedisClient();
        double start = System.currentTimeMillis();
        for(int i=0;i<1000;++i){
            commandExecute(client,"HSET myHt key"+i+" "+i);
        }
        double end = System.currentTimeMillis();
        System.out.println(end-start);

        start = System.currentTimeMillis();
        for(int i=0;i<1000;++i){
            commandExecute(client,"HGET myHt key"+i);
        }
        end = System.currentTimeMillis();
        System.out.println(end-start);

    }
}
