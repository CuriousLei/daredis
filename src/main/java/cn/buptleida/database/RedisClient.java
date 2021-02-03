package cn.buptleida.database;

import cn.buptleida.conf.Command;
import cn.buptleida.dataCoreObj.base.CmdExecutor;
import cn.buptleida.nio.clihdl.ClientHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RedisClient implements CmdExecutor {

    private RedisDB db;//该客户端的目标数据库
    private boolean isFake;//是否为假客户端
    private RedisClientCallBack callBack;//用来向nio中的客户端处理器回调消息
    public RedisClient(){
        //使用默认数据库
        this.db = RedisServer.INSTANCE.db[0];
        this.isFake = false;
    }
    public RedisClient(int index){
        //使用指定数据库
        this.db = RedisServer.INSTANCE.db[index];
    }
    public void msgReturn(Object msg){
        if(!isFake) callBack.RedisMsgCallBack(msg);
    }
    public RedisDB getDb() {
        return db;
    }

    public void setDb(RedisDB db) {
        this.db = db;
    }
    public void setDbByIndex(int index){
        this.db = RedisServer.INSTANCE.db[index];
    }

    public boolean isFake() {
        return isFake;
    }

    public void setFake(boolean fake) {
        isFake = fake;
    }

    public RedisClientCallBack getClientHandlerCallBack() {
        return callBack;
    }

    public void setClientHandlerCallBack(RedisClientCallBack callBack) {
        this.callBack = callBack;
    }

    public interface RedisClientCallBack{
        void RedisMsgCallBack(Object msg);
    }

    @Override
    public Object proc(String[] params, Command cmd, Method method, RedisClient client) {
        return null;
    }

    public void select(int index){
        this.db = RedisServer.INSTANCE.db[0];
    }
}
