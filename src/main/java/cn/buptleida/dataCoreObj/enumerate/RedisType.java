package cn.buptleida.dataCoreObj.enumerate;

public enum RedisType {
    STRING(0),
    LIST(1),
    HASH(2),
    SET(3),
    ZSET(4);
    private final int val;

    RedisType(int VAL) {
        this.val = VAL;
    }
    public int VAL(){
        return val;
    }
}
