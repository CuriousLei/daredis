package cn.buptleida.database;

public class RedisClient {

    //该客户端的目标数据库
    private RedisDB db;
    private boolean isFake;

    public RedisClient(){
        //使用默认数据库
        this.db = RedisServer.db[0];
        this.isFake = false;
    }
    public RedisClient(int index){
        //使用指定数据库
        this.db = RedisServer.db[index];
    }
    public RedisDB getDb() {
        return db;
    }

    public void setDb(RedisDB db) {
        this.db = db;
    }
    public void setDbByIndex(int index){
        this.db = RedisServer.db[index];
    }

    public boolean isFake() {
        return isFake;
    }

    public void setFake(boolean fake) {
        isFake = fake;
    }
}
