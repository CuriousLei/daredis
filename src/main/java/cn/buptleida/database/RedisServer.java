package cn.buptleida.database;

import cn.buptleida.dataCoreObj.underObj.SDS;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class RedisServer {

    private int dbNum = 16;
    private RedisDB[] db;

    RedisServer(){
        this.db = new RedisDB[this.dbNum];
    }

    public void commandExcute(RedisClient client) throws Exception {
        RedisDB database = client.getDb();
        Class<RedisDB> redisDBClass = RedisDB.class;
        Method method = redisDBClass.getMethod("SET", String.class, String.class);
        method.invoke(database,"name", "leida");
    }
    public static void main(String[] args) throws Exception{

    }
}
