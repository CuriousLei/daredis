package cn.buptleida.dataCoreObj.enumerate;

public enum RedisEnc {
    RAW(0),
    INT(1),
    HT(2),
    ZIPMAP(3),
    LINKEDLIST(4),
    ZIPLIST(5),
    INTSET(6),
    SKIPLIST(7),
    EMBSTR(8);
    private final int val;

    RedisEnc(int VAL) {
        this.val = VAL;
    }
    public int VAL(){
        return val;
    }
}
