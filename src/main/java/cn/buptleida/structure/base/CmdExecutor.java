package cn.buptleida.structure.base;

import cn.buptleida.conf.Command;
import cn.buptleida.database.RedisClient;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface CmdExecutor {
    Object proc(String[] params, Command cmd, Method method, RedisClient client) throws InvocationTargetException, IllegalAccessException;
}
