package cn.buptleida.database;

public class RedisClient {

    //该客户端的目标数据库
    private RedisDB db;

    public RedisClient(){
        //使用默认数据库
        this.db = RedisServer.db[0];
    }
    public RedisClient(int index){
        //使用指定数据库
        this.db = RedisServer.db[index];
    }
    public RedisDB getDb() {
        return db;
    }
}
