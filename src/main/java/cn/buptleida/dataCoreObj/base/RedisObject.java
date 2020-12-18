package cn.buptleida.dataCoreObj.base;


//五种基本对象结构的父类
public abstract class RedisObject {

    protected int type;
    protected int encoding;
    protected RedisObj ptr;

    public int getType() {
        return type;
    }

    public int getEncoding() {
        return encoding;
    }
}
