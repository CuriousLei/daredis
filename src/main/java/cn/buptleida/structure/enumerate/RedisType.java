package cn.buptleida.structure.enumerate;

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
    public String getDescByVal(int val){
        switch (val){
            case 0 : return "String";
            case 1 : return "List";
            case 2 : return "HashTable";
            case 3 : return "Set";
            case 4 : return "ZSet";
        }
        return null;
    }
}
