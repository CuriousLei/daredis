package cn.buptleida.dataCoreObj.base;


import cn.buptleida.conf.Command;
import cn.buptleida.database.RedisClient;
import cn.buptleida.util.ConvertUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

//五种基本对象结构的父类
public abstract class RedisObject implements CmdExecutor {

    protected int type;
    protected int encoding;
    protected RedisObj ptr;

    public int getType() {
        return type;
    }

    public int getEncoding() {
        return encoding;
    }

    @Override
    public Object proc(String[] params, Command cmd, Method method, RedisClient client) throws InvocationTargetException, IllegalAccessException {

        return null;
    }
}
