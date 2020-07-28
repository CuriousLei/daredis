package cn.buptleida.database;

import cn.buptleida.dataCoreObj.RedisString;
import cn.buptleida.dataCoreObj.base.RedisObject;
import cn.buptleida.dataCoreObj.enumerate.RedisType;
import cn.buptleida.dataCoreObj.enumerate.Status;
import cn.buptleida.dataCoreObj.underObj.Dict;
import cn.buptleida.dataCoreObj.underObj.SDS;

public class RedisDB {

    //数据库的键空间
    Dict<SDS, RedisObject> dict;

    public RedisDB() {
        this.dict = new Dict<>();
    }

    //String相关命令



    /**
     * 插入键值对
     *
     * @param key
     * @param val
     */
    public int SET(String key, String val) {
        SDS keySds = new SDS(key.toCharArray());

        RedisString valStr = new RedisString(val);
        dict.put(keySds, valStr);

        return Status.SUCCESS;
    }

    /**
     * 根据键获取对应的值
     *
     * @param key
     * @return
     */
    public String GET(String key) {
        RedisString val = getValByKey(key);
        if(val == null) return null;
        return val.get();
    }

    /**
     * 拼接字符串
     *
     * @param key
     * @param extStr
     * @return 返回新字符串的长度
     */
    public int APPEND(String key, String extStr) {
        RedisString val = getValByKey(key);
        if(val == null){//不存在则直接SET一个新字符串
            SET(key,extStr);
            return extStr.length();
        }

        return val.append(extStr);
    }

    /**
     * 根据给定键，返回对应的字符串长度
     * @param key
     * @return
     */
    public int STRLEN(String key) {
        RedisString val = getValByKey(key);

        return val.strlen();
    }

    /**
     * 将 key 中储存的数字值增一
     * @param key
     * @return 增一后的结果
     */
    public long INCR(String key){
        RedisString val = getValByKey(key);
        if(val==null){
            SET(key,"0");
            return INCR(key);
        }
        return val.incrby();
    }
    /**
     * 将 key 中储存的数字值增一
     * @param key
     * @return 增一后的结果
     */
    public long DECR(String key){
        RedisString val = getValByKey(key);
        if(val==null){
            SET(key,"0");
            return DECR(key);
        }
        return val.decrby();
    }

    private RedisString getValByKey(String key) {
        SDS keySds = new SDS(key.toCharArray());

        RedisObject val = dict.get(keySds);
        if (val == null) return null;
        if (val.getType() != RedisType.STRING.VAL())
            return null;
        return (RedisString) val;
    }
}
