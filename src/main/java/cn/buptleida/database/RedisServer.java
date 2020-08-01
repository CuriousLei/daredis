package cn.buptleida.database;

import cn.buptleida.dataCoreObj.underObj.SDS;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Arrays;

public class RedisServer {

    private static int dbNum = 16;
    public static RedisDB[] db;

    public static void init(){
        db = new RedisDB[dbNum];
    }

    public static void initDB(int index){
        db[index] = new RedisDB();
    }

    public static String commandExecute(RedisClient client, String command) throws Exception {

        String[] commands = command.split(" ");
        String commandName = commands[0];
        String[] params = Arrays.copyOfRange(commands,1,commands.length);

        Object database = client.getDb();
        Class<RedisDB> redisDBClass = RedisDB.class;
        Method method = redisDBClass.getMethod(commandName, String[].class);

        Object returnValue =  method.invoke(database,(Object) params);
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

        String[] l = new String[]{"ioio","uiuy"};
        System.out.println(Arrays.toString(l));
        //commandExcute(null,"SET name leida");
        // Class<RedisServer> redisServerClass = RedisServer.class;
        // Method method = redisServerClass.getMethod("test",String[].class);
        // String[] strs = new String[]{"name","leida","qihang"};
        // Object o = redisServerClass.newInstance();
        // method.invoke(o,(Object) strs);
    }
}
